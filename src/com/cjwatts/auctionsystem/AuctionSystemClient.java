package com.cjwatts.auctionsystem;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import com.cjwatts.auctionsystem.alert.Alerter;
import com.cjwatts.auctionsystem.entity.Category;
import com.cjwatts.auctionsystem.entity.Item;
import com.cjwatts.auctionsystem.entity.User;
import com.cjwatts.auctionsystem.exception.NotLoggedInException;
import com.cjwatts.auctionsystem.gui.*;
import com.cjwatts.auctionsystem.io.Comms;
import com.cjwatts.auctionsystem.message.*;
import com.cjwatts.auctionsystem.security.PasswordHasher;
// TODO: Fix filter switching glitch
// TODO: Win notifications
public class AuctionSystemClient extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private static final ScheduledExecutorService auctionUpdater = Executors.newSingleThreadScheduledExecutor();

	// Menu screen content panes
	private JPanel login, register, auctions, auctionList, itemView;

	private String userId;
	private User userProfile;
	private Map<Integer, Item> currentAuctions = new HashMap<>();
	private Integer lastShownItem;
	
	private ViewRequest activeRequest = new ViewRequest();

	public static void main(String[] args) {
		final AuctionSystemClient system = new AuctionSystemClient("Auction System");
		
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				GUIAlert alerts = new GUIAlert(system);
				Alerter.setHandler(alerts);
				system.init();
				system.showLogin();
				system.setVisible(true);
				try {
					Comms.setRemoteAddr(InetAddress.getByName("localhost"));
				} catch (UnknownHostException ex) {
					Alerter.getHandler().fatal("Communication Error", "Could not find server.");
				}
			}
		});
		
		auctionUpdater.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				synchronized (system.activeRequest) {
					system.activeRequest.setAutomatic(true);
					system.updateAuctionList();
					system.activeRequest.setAutomatic(false);
				}
			}
		}, 1, 1, TimeUnit.MINUTES);
	}
	
	public AuctionSystemClient() {
		super();
	}
	
	public AuctionSystemClient(String title) {
		super(title);
	}

	public void init() {
		setUpLogin();
		setUpAuctions();

		// Frame options
		this.pack();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(new Dimension(800, 600));
	}
	
	/**
	 * Initialise the login form
	 */
	private void setUpLogin() {
		setUpRegister();
		login = new JPanel();

		// Controls
		final JTextField username = new PlaceholderTextField("username");
		final JPasswordField password = new PlaceholderPasswordField("password");
		JButton submit = new JButton("Login");
		JButton register = new JButton("Register");

		JPanel centreBox = new JPanel();
		centreBox.setPreferredSize(new Dimension(200, 110));
		centreBox.setMinimumSize(new Dimension(200, 110));
		centreBox.setMaximumSize(new Dimension(200, 110));

		/*
		 * Add button listeners
		 */
		submit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Send request with username and password
				AuthenticateRequest rq = new AuthenticateRequest();

				userId = username.getText();
				char[] pw = password.getPassword();
				password.setText("");
				byte[] hash;

				PasswordHasher ph = new PasswordHasher();
				hash = ph.preHash(userId, new String(pw));
				// Clear password array for extra security
				Arrays.fill(pw, '\0');
				rq.setSenderId(userId);
				rq.setPassword(hash);

				boolean success = Comms.sendMessage(rq);

				if (success) {
					// Receive response and handle
					try {
						AuthenticateResponse re = (AuthenticateResponse) Comms.receiveMessage();
						handle(re);
					} catch (ClassCastException ex) {
						Alerter.getHandler().severe("Authentication failure",
										"Response from server was malformed. Please try again.");
					}
				}
			}
		});
		
		register.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showRegister();
			}
		});

		// Layout code
		login.setLayout(new BoxLayout(login, BoxLayout.X_AXIS));
		login.add(Box.createHorizontalGlue());
		login.add(centreBox);
		login.add(Box.createHorizontalGlue());

		GroupLayout loginControlLayout = new GroupLayout(centreBox);
		loginControlLayout.setHorizontalGroup(loginControlLayout.createSequentialGroup()
			.addContainerGap()
			.addGroup(loginControlLayout.createParallelGroup()
				.addComponent(username)
				.addComponent(password)
				.addGroup(loginControlLayout.createSequentialGroup()
					.addComponent(submit, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(register, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				)
			)
			.addContainerGap()
		);
		loginControlLayout.setVerticalGroup(loginControlLayout.createSequentialGroup()
			.addComponent(username)
			.addPreferredGap(ComponentPlacement.RELATED)
			.addComponent(password)
			.addPreferredGap(ComponentPlacement.UNRELATED)
			.addGroup(loginControlLayout.createParallelGroup()
				.addComponent(submit)
				.addComponent(register)
			)
		);
		centreBox.setLayout(loginControlLayout);
	}
	
	/**
	 * Initialise registration form
	 */
	private void setUpRegister() {
		register = new JPanel();
		
		// Controls
		final JTextField firstname = new PlaceholderTextField("first name");
		final JTextField lastname = new PlaceholderTextField("last name");
		final JTextField username = new PlaceholderTextField("username");
		final JPasswordField password = new PlaceholderPasswordField("password");
		final JPasswordField cpassword = new PlaceholderPasswordField("confirm password");
		JButton submit = new JButton("Register");
		JButton cancel = new JButton("Cancel");

		JPanel centreBox = new JPanel();
		centreBox.setPreferredSize(new Dimension(200, 200));
		centreBox.setMinimumSize(new Dimension(200, 200));
		centreBox.setMaximumSize(new Dimension(200, 200));
		
		/*
		 * Add button listeners
		 */
		submit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RegisterRequest rq = new RegisterRequest();
				
				userId = username.getText();
				char[] pw = password.getPassword();
				char[] cpw = cpassword.getPassword();
				password.setText("");
				cpassword.setText("");
				byte[] hash;

				if (!Arrays.equals(pw, cpw)) {
					// Clear password arrays for extra security
					Arrays.fill(pw, '\0');
					Arrays.fill(cpw, '\0');
					Alerter.getHandler().warning("Registration",
							"Passwords did not match. Please try again.");
				} else {
					Arrays.fill(cpw, '\0');
					PasswordHasher ph = new PasswordHasher();
					hash = ph.preHash(userId, new String(pw));
					Arrays.fill(pw, '\0');
					
					rq.setSenderId(userId);
					rq.setPassword(hash);
					
					User user = new User();
					user.setFirstName(firstname.getText());
					user.setLastName(lastname.getText());
					rq.setUser(user);
	
					boolean success = Comms.sendMessage(rq);
	
					if (success) {
						// Receive response and handle
						try {
							RegisterResponse re = (RegisterResponse) Comms.receiveMessage();
							handle(re);
						} catch (ClassCastException ex) {
							Alerter.getHandler().severe("Authentication failure",
											"Response from server was malformed. Please try again.");
						}
					}
				}
			}
		});
		
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showLogin();
			}
		});
		
		// Layout code
		register.setLayout(new BoxLayout(register, BoxLayout.X_AXIS));
		register.add(Box.createHorizontalGlue());
		register.add(centreBox);
		register.add(Box.createHorizontalGlue());

		GroupLayout registerControlLayout = new GroupLayout(centreBox);
		registerControlLayout.setHorizontalGroup(registerControlLayout.createSequentialGroup()
			.addContainerGap()
			.addGroup(registerControlLayout.createParallelGroup()
				.addGroup(registerControlLayout.createSequentialGroup()
					.addComponent(firstname)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lastname)
				)
				.addComponent(username)
				.addComponent(password)
				.addComponent(cpassword)
				.addGroup(registerControlLayout.createSequentialGroup()
					.addComponent(submit, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(cancel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				)
			)
			.addContainerGap()
		);
		registerControlLayout.setVerticalGroup(registerControlLayout.createSequentialGroup()
			.addGroup(registerControlLayout.createParallelGroup()
				.addComponent(firstname)
				.addComponent(lastname)
			)
			.addPreferredGap(ComponentPlacement.UNRELATED)
			.addComponent(username)
			.addPreferredGap(ComponentPlacement.RELATED)
			.addComponent(password)
			.addPreferredGap(ComponentPlacement.RELATED)
			.addComponent(cpassword)
			.addPreferredGap(ComponentPlacement.UNRELATED)
			.addGroup(registerControlLayout.createParallelGroup()
				.addComponent(submit)
				.addComponent(cancel)
			)
		);
		centreBox.setLayout(registerControlLayout);
	}

	/**
	 * Initialise the auctions form
	 */
	private void setUpAuctions() {
		setUpAuctionList();
		setUpItemView();
		
		/*
		 * Side bars
		 */
		SideBar auctionSideBar = new SideBar();
		SideBar sellingSideBar = new SideBar();
		SideBar buyingSideBar = new SideBar();
		
		JPanel auctionFilters = new JPanel();
		JPanel sellingMenu = new JPanel();
		JPanel buyingMenu = new JPanel();
		
		auctionSideBar.setControls(auctionFilters);
		sellingSideBar.setControls(sellingMenu);
		buyingSideBar.setControls(buyingMenu);
		
		auctionSideBar.setUpdater(new Runnable() {
			@Override
			public void run() {
				// Refresh all auctions
				activeRequest.setCategory(null);
				activeRequest.setItemId(null);
				activeRequest.setStartTime(null);
				activeRequest.setVendor(null);
				updateAuctionList();
			}
		});
		
		sellingSideBar.setUpdater(new Runnable() {
			@Override
			public void run() {
				// Give all auctions being sold by this user
				activeRequest.setCategory(null);
				activeRequest.setItemId(null);
				activeRequest.setStartTime(null);
				activeRequest.setVendor(userId);
				updateAuctionList();
			}
		});
		
		buyingSideBar.setUpdater(new Runnable() {
			@Override
			public void run() {
				// Give all auctions being bid on by this user from all time
				activeRequest.setBidder(userId);
				activeRequest.setCategory(null);
				activeRequest.setItemId(null);
				activeRequest.setStartTime(new Date(0));
				activeRequest.setVendor(null);
				updateAuctionList();
			}
		});
		
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Auctions", auctionSideBar);
		tabs.addTab("Selling", sellingSideBar);
		tabs.addTab("Buying", buyingSideBar);
		
		// The listings scroller is shared between all tabs using
		// this little remove-then-add hack
		final JScrollPane auctionScroller = new JScrollPane(
				auctionList,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		tabs.addChangeListener(new ChangeListener() {
			private SideBar previous;
			
			@Override
			public void stateChanged(ChangeEvent e) {
				try {
					JTabbedPane source = ((JTabbedPane) e.getSource());
					if (source.getSelectedIndex() >= 0) {
						SideBar sb = (SideBar) source.getComponentAt(source.getSelectedIndex());
						
						if (previous != null) previous.removeListings();
						sb.setListings(auctionScroller);
						sb.updateListings();
						previous = sb;
					}
				} catch (ClassCastException ex) {
					Alerter.getHandler().severe("Interface Error", "Something went wrong with the tabs! Please try again.");
				}
			}
		});
		tabs.setSelectedIndex(-1);
		tabs.setSelectedIndex(0);
		
		/*
		 * Create auction filters menu
		 */
		JLabel auctionsTitle = new JLabel("<html><h3>Current Auctions</h3>Filter auctions by...</html>");
		final PlaceholderComboBox category = new PlaceholderComboBox("category");
		for (Category c : Category.values()) {
			category.addItem(c);
		}
		final JTextField itemid = new PlaceholderTextField("item id");
		final JTextField seller = new PlaceholderTextField("seller");
		final JCheckBox enableStart = new JCheckBox();
		final JLabel startLbl = new JLabel("Start time");
		final DateSpinner start = new DateSpinner();

		/*
		 * Set up automatic re-searching
		 */
		class FilterChangeListener implements DocumentListener, ActionListener, ChangeListener {
			private final ScheduledExecutorService updater = 
					  Executors.newSingleThreadScheduledExecutor();
			private Future<?> future;
			
			@Override
			public void changedUpdate(DocumentEvent arg0) {}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				update();
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				update();
			}
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				update();
			}
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				update();
			}
			
			private void update() {
				synchronized (activeRequest) {
					if (category.getRealSelectedItem() != null) {
						activeRequest.setCategory((Category) category.getRealSelectedItem());
					} else {
						activeRequest.setCategory(null);
					}
					
					if (enableStart.isSelected()) {
						start.setEnabled(true);
						activeRequest.setStartTime(start.getValue());
					} else {
						start.setEnabled(false);
						activeRequest.setStartTime(null);
					}
					
					if (!seller.getText().equals("")) {
						activeRequest.setVendor(seller.getText());
					} else {
						activeRequest.setVendor(null);
					}
					
					if (!itemid.getText().equals("")) {
						try {
							activeRequest.setItemId(Integer.parseInt(itemid.getText()));
						} catch (NumberFormatException ex) {
							Alerter.getHandler().warning("Auction Filter", itemid.getText() + " is not a valid id.");
						}
					} else {
						activeRequest.setItemId(null);
					}
				}
				
				// Wait for the user to finish typing first
				if (future != null) {
					future.cancel(false);
				}
				future = updater.schedule(new Runnable() {
					@Override
					public void run() {
						updateAuctionList();
					}
				}, 500, TimeUnit.MILLISECONDS);
			}
		}
		FilterChangeListener l = new FilterChangeListener();
		category.addActionListener(l);
		seller.getDocument().addDocumentListener(l);
		itemid.getDocument().addDocumentListener(l);
		enableStart.addChangeListener(l);
		start.addChangeListener(l);
		
		// Layout code
		GroupLayout auctionFilterLayout = new GroupLayout(auctionFilters);
		auctionFilterLayout.setHorizontalGroup(auctionFilterLayout.createSequentialGroup()
			.addContainerGap()
			.addGroup(auctionFilterLayout.createParallelGroup()
				.addComponent(auctionsTitle)
				.addComponent(category)
				.addComponent(seller)
				.addComponent(itemid)
				.addGroup(auctionFilterLayout.createSequentialGroup()
					.addComponent(enableStart)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(startLbl)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(start)
				)
			)
			.addContainerGap()
		);
		auctionFilterLayout.setVerticalGroup(auctionFilterLayout.createSequentialGroup()
			.addContainerGap()
			.addComponent(auctionsTitle)
			.addPreferredGap(ComponentPlacement.UNRELATED)
			.addComponent(category)
			.addPreferredGap(ComponentPlacement.RELATED)
			.addComponent(seller)
			.addPreferredGap(ComponentPlacement.RELATED)
			.addComponent(itemid)
			.addPreferredGap(ComponentPlacement.RELATED)
			.addGroup(auctionFilterLayout.createParallelGroup()
				.addComponent(enableStart)
				.addComponent(startLbl)
				.addComponent(start)
			)
			.addContainerGap()
		);
		auctionFilters.setLayout(auctionFilterLayout);
			
		/*
		 * Create selling menu
		 */
		JLabel sellingMenuTitle = new JLabel("<html><h3>Your Selling Items</h3></html>");
		JButton sellNew = new JButton("Sell new item");
		sellNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(AuctionSystemClient.this);

				JPanel newItem = new JPanel();
				final JTextField title = new PlaceholderTextField("title");
				final JTextArea description = new PlaceholderTextArea("description");
				JScrollPane descriptionScroll = new JScrollPane(
						description,
						JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				final PlaceholderComboBox category = new PlaceholderComboBox("category");
				for (Category c : Category.values()) {
					category.addItem(c);
				}
				
				final JTextField image = new PlaceholderTextField("display image");
				final JButton browse = new JButton("Browse...");
				final JFileChooser fileChooser = new JFileChooser();
				
				final JLabel startLbl = new JLabel("Start time");
				final JLabel endLbl = new JLabel("End time");
				final DateSpinner start = new DateSpinner();
				final DateSpinner end = new DateSpinner();
				final JButton submit = new JButton("Submit");
				final JButton cancel = new JButton("Cancel");
				
				final JDialog dialog = new JDialog(parent, true);
				
				/*
				 * Set up image chooser
				 */
				browse.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						final HashSet<String> accepted = new HashSet<>();
						accepted.add("jpg");
						accepted.add("jpeg");
						accepted.add("png");
						accepted.add("bmp");
						accepted.add("gif");
						
						FileFilter imageFilter = new FileFilter() {
							@Override
							public boolean accept(File f) {
								if (f.isDirectory()) return true;
								
								int i = f.getName().lastIndexOf('.');
								if (i > 0) {
								    return accepted.contains(f.getName().toLowerCase().substring(i+1));
								} else {
									return false;
								}
							}

							@Override
							public String getDescription() {
								return "Image Files";
							}
						};
						fileChooser.setFileFilter(imageFilter);
						
						if (fileChooser.showDialog(parent, "Upload") == JFileChooser.APPROVE_OPTION) {
							image.setText(fileChooser.getSelectedFile().toString());
						}
					}
				});
				
				/*
				 * Add button listeners
				 */
				submit.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						Item item = new Item();
						
						/*
						 * Parse and validate the image
						 */
						try {
							BufferedImage img = ImageIO.read(new File(image.getText()));
							
							// Shrink the image to 200x200
							BufferedImage newImg = new BufferedImage(200, 200, img.getType());
							Graphics2D g2 = (Graphics2D) newImg.getGraphics();
							g2.drawImage(img, 0, 0, 200, 200, null);
							g2.dispose();
							
							// Set it to the item
							item.setImage(newImg);
						} catch (IOException ex) {
							Alerter.getHandler().warning("Image Upload", ex.getMessage());
							return;
						}
						
						/*
						 * Parse and validate the start/end dates
						 */
						// Check if start is in the present or the future
						// (plus 5 minutes of courtesy time)
						if (start.getValue().getTime() < new Date().getTime() - 300000) {
							Alerter.getHandler().warning("Auction Submission", "Auctions cannot start in the past!");
							return;
						}
						// Check if end is after start
						if (end.getValue().before(start.getValue())) {
							Alerter.getHandler().warning("Auction Submission", "The ending time must be after the starting time!");
							return;
						}
						
						// If all went well, continue
						item.setStart(start.getValue());
						item.setEnd(end.getValue());
						
						item.setCategory((Category) category.getRealSelectedItem());
						item.setDescription(description.getText());
						item.setTitle(title.getText());
						item.setVendor(userId);
						
						CreateItemRequest rq = new CreateItemRequest();
						rq.setSenderId(userId);
						rq.setSessionToken(userProfile.getSessionToken());
						rq.setItem(item);			
						
						boolean success = Comms.sendMessage(rq);

						if (success) {
							// Receive response and handle
							try {
								CreateItemResponse re = (CreateItemResponse) Comms.receiveMessage();
								handle(re);
							} catch (ClassCastException ex) {
								Alerter.getHandler().severe("Item Creation Failure",
												"Response from server was malformed. Please try again.");
							}
						}
						
						dialog.setVisible(false);
					}
				});
				
				cancel.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						dialog.setVisible(false);
					}
				});
				
				// Layout code
				GroupLayout newItemLayout = new GroupLayout(newItem);
				newItemLayout.setHorizontalGroup(newItemLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(newItemLayout.createParallelGroup()
						.addComponent(title)
						.addComponent(descriptionScroll)
						.addComponent(category)
						.addGroup(newItemLayout.createSequentialGroup()
							.addComponent(image)
							.addComponent(browse)
						)
						.addGroup(newItemLayout.createSequentialGroup()
							.addGroup(newItemLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(startLbl)
								.addComponent(endLbl)
							)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(newItemLayout.createParallelGroup()
								.addComponent(start)
								.addComponent(end)
							)
						)
						.addGroup(newItemLayout.createSequentialGroup()
							.addComponent(submit, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(cancel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						)
					)
					.addContainerGap()
				);
				newItemLayout.setVerticalGroup(newItemLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(title)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(descriptionScroll, 100, 100, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(category, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(newItemLayout.createParallelGroup()
						.addComponent(image, 28, 28, 28)
						.addComponent(browse)
					)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(newItemLayout.createParallelGroup()
						.addComponent(startLbl)
						.addComponent(start, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(newItemLayout.createParallelGroup()
						.addComponent(endLbl)
						.addComponent(end, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(newItemLayout.createParallelGroup()
						.addComponent(submit)
						.addComponent(cancel)
					)
					.addContainerGap()
				);
				newItem.setLayout(newItemLayout);
				
				dialog.setTitle("New Item");
				dialog.add(newItem);
				dialog.setSize(new Dimension(300, 380));
				dialog.setLocationRelativeTo(parent);
				dialog.setVisible(true);
			}
		});
		sellingMenu.setLayout(new BorderLayout());
		sellingMenu.add(sellingMenuTitle, BorderLayout.NORTH);
		sellingMenu.add(sellNew, BorderLayout.CENTER);
		
		/*
		 * Create buying menu
		 */
		JLabel buyingMenuTitle = new JLabel("<html><h3>Your Recent Bids</h3></html>");
		buyingMenu.setLayout(new BorderLayout());
		buyingMenu.add(buyingMenuTitle, BorderLayout.NORTH);
		
		/*
		 * Create auctions frame
		 */
		auctions = new JPanel();
		auctions.setLayout(new BorderLayout());
		auctions.add(tabs, BorderLayout.WEST);
		auctions.add(itemView, BorderLayout.CENTER);
	}
	
	/**
	 * Initialise auction list
	 */
	private void setUpAuctionList() {
		auctionList = new JPanel();
		auctionList.setPreferredSize(new Dimension(250, 0));
		auctionList.setLayout(new FlowLayout());
	}

	/**
	 * Initialise auction viewing window
	 */
	private void setUpItemView() {
		itemView = new JPanel();
		itemView.setBackground(Color.WHITE);
		itemView.setLayout(new BorderLayout());
	}

	/**
	 * Swap the current content pane with the new container.
	 * Keeps track of previous window also.
	 * @param contentPane
	 */
	private void show(Container contentPane) {
		this.setContentPane(contentPane);
		this.revalidate();
		this.repaint();
	}
	
	public void showLogin() {
		userId = null;
		userProfile = null;
		show(login);
	}
	
	public void showRegister() {
		userId = null;
		userProfile = null;
		show(register);
	}

	public void showAuctions() {
		updateAuctionList();
		show(auctions);
	}

	/**
	 * Reads item data and displays it on a new page
	 * @param i The ID of the item to be viewed
	 */
	public void showItem(final Integer i) {
		lastShownItem = i;
		Item retrieved = currentAuctions.get(i);
		ItemPanel panel = new ItemPanel(retrieved);
		panel.addItemPanelListener(new ItemPanelListener() {
			@Override
			public void bidSubmitted(BidEvent e) {
				BidRequest rq = new BidRequest();
				rq.setSenderId(userId);
				rq.setSessionToken(userProfile.getSessionToken());
				rq.setItemId(i);
				rq.setBid(e.getBid());

				boolean success = Comms.sendMessage(rq);

				if (success) {
					// Receive response and handle
					try {
						BidResponse re = (BidResponse) Comms.receiveMessage();
						handle(re);
					} catch (ClassCastException ex) {
						Alerter.getHandler().severe("Bid Failure",
										"Response from server was malformed. Please try again.");
					}
				}
			}
		});
		itemView.removeAll();
		itemView.add(panel, BorderLayout.NORTH);
		itemView.revalidate();
		itemView.repaint();
	}
	
	/**
	 * Refreshes the item view
	 */
	public void reshowItem() {
		showItem(lastShownItem);
	}
	
	/**
	 * Updates the auction list with the request specified by the
	 * activeRequest instance variable.
	 * 
	 * Sender ID and session token do not need to be specified - 
	 * these will be filled in automatically.
	 */
	public void updateAuctionList() {
		new Thread() {
			@Override
			public void run() {
				if (userId != null && userProfile != null) {
					synchronized (activeRequest) {
						activeRequest.setSenderId(userId);
						activeRequest.setSessionToken(userProfile.getSessionToken());
						
						boolean success = Comms.sendMessage(activeRequest);
				
						if (success) {
							// Receive response and handle
							try {
								ViewResponse re = (ViewResponse) Comms.receiveMessage();
								handle(re);
							} catch (ClassCastException ex) {
								Alerter.getHandler().severe("Auction Listing Failure",
												"Response from server was malformed. Please try again.");
							}
						}

						// Update GUI
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								auctionList.removeAll();
								for (final Integer i : currentAuctions.keySet()) {
									ListingPanel l = new ListingPanel(userId, currentAuctions.get(i));
									l.addMouseListener(new MouseAdapter() {
										@Override
										public void mousePressed(MouseEvent e) {
											showItem(i);
										}
									});
									auctionList.add(l);
								}
								auctionList.revalidate();
								auctionList.repaint();
							}
						});
					}
				}
			}
		}.start();
	}
	
	/**
	 * Check that a new token has been returned
	 * and update the session and user profile accordingly.
	 * 
	 * @param re Response from server after last transaction
	 * @return True if session is valid and updated, otherwise false
	 */
	private boolean updateSession(Response re) {
		byte[] token = re.getSessionToken();

		try {
			if (token == null || token.length == 0) {
				throw new NotLoggedInException("Your session has expired. Please log in again to continue.");
			}
			
			if (userProfile != null) {
				if (!Arrays.equals(token, userProfile.getSessionToken())) {
					userProfile.setSessionToken(token);
				} else {
					// If the new token is the same as the old one, something went wrong
					throw new NotLoggedInException("Your session appears to be invalid. You have been logged out for security reasons. Please log in again to continue");
				}
			} else if (userId != null) {
				// If the user profile isn't populated, but the user id is known
				// then call an update request, and pull in the new profile data
				// from the response
				UpdateProfileRequest rq = new UpdateProfileRequest();
				rq.setSenderId(userId);
				rq.setSessionToken(token);
				
				boolean success = Comms.sendMessage(rq);

				if (success) {
					// Receive response and handle
					try {
						UpdateProfileResponse res = (UpdateProfileResponse) Comms.receiveMessage();
						handle(res);
					} catch (ClassCastException ex) {
						Alerter.getHandler().severe("Profile Update Failure",
										"Response from server was malformed. Please try again.");
					}
				}
			} else {
				throw new NotLoggedInException("You don't appear to have logged in to the system. Please try again.");
			}
			
			return true;
		} catch (NotLoggedInException ex) {
			showLogin();
			Alerter.getHandler().warning("Session Timeout", ex.getMessage());
			return false;
		}
	}

	/**
	 * Receives and handles an AuthenticateResponse.
	 * 
	 * @param re
	 */
	public synchronized void handle(AuthenticateResponse re) {
		if (re != null) {
			if (re.isSuccess()) {
				if (updateSession(re) && this.getContentPane() == login) {
					showAuctions();
				}
			} else {
				showLogin();
				Alerter.getHandler().warning("Authentication Failure", re.getMessage());
			}
		}
	}

	/**
	 * Receives and handles a BidResponse.
	 * 
	 * @param re
	 */
	public synchronized void handle(BidResponse re) {
		if (re != null && updateSession(re)) {
			if (re.isSuccess()) {
				Alerter.getHandler().info("Item Bid", "Your bid was successful.");
				updateAuctionList();
				reshowItem();
			} else {
				Alerter.getHandler().warning("Item Bid", re.getMessage());
			}
		}
	}

	/**
	 * Receives and handles a CreateItemResponse.
	 * 
	 * @param re
	 */
	public synchronized void handle(CreateItemResponse re) {
		if (re != null && updateSession(re)) {
			if (re.isSuccess()) {
				Alerter.getHandler().info("Item Submission", "Your submission was successful.");
				updateAuctionList();
			} else {
				Alerter.getHandler().warning("Item Submission", re.getMessage());
			}
		}
	}

	/**
	 * Receives and handles a RegisterResponse.
	 * 
	 * @param re
	 */
	public synchronized void handle(RegisterResponse re) {
		if (re != null) {
			if (re.isSuccess()) {
				if (updateSession(re) && this.getContentPane() == register) {
					showAuctions();
				}
			} else {
				showLogin();
				Alerter.getHandler().warning("Registration Failure", re.getMessage());
			}
		}
	}

	/**
	 * Receives and handles an UpdateProfileResponse.
	 * 
	 * @param re
	 */
	public synchronized void handle(UpdateProfileResponse re) {
		if (re != null) {
			if (re.isSuccess()) {
				this.userProfile = re.getProfile();
				updateSession(re);
			} else {
				if (updateSession(re)) {
					Alerter.getHandler().warning("Profile Update Failure", re.getMessage());
				}
			}
		}
	}
	
	/**
	 * Receives and handles a ViewResponse.
	 * 
	 * @param re
	 */
	public synchronized void handle(ViewResponse re) {
		if (re != null && updateSession(re)) {
			if (re.isSuccess()) {
				currentAuctions = re.getItems();
				activeRequest.clearFilters();
			} else {
				Alerter.getHandler().warning("Auction Listing Failure", re.getMessage());
			}
		}
	}
}

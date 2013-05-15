package com.cjwatts.auctionsystem;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.*;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.cjwatts.auctionsystem.alert.Alerter;
import com.cjwatts.auctionsystem.entity.Category;
import com.cjwatts.auctionsystem.entity.Item;
import com.cjwatts.auctionsystem.entity.User;
import com.cjwatts.auctionsystem.exception.NotLoggedInException;
import com.cjwatts.auctionsystem.gui.GUIAlert;
import com.cjwatts.auctionsystem.gui.ItemPanel;
import com.cjwatts.auctionsystem.gui.BidEvent;
import com.cjwatts.auctionsystem.gui.ItemPanelListener;
import com.cjwatts.auctionsystem.gui.ListingPanel;
import com.cjwatts.auctionsystem.gui.PlaceholderComboBox;
import com.cjwatts.auctionsystem.gui.PlaceholderPasswordField;
import com.cjwatts.auctionsystem.gui.PlaceholderTextField;
import com.cjwatts.auctionsystem.io.Comms;
import com.cjwatts.auctionsystem.message.*;
import com.cjwatts.auctionsystem.security.PasswordHasher;

public class AuctionSystemClient extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private static final ScheduledExecutorService auctionUpdater = Executors.newSingleThreadScheduledExecutor();

	// Menu screen content panes
	private JPanel login, register, auctions, auctionList, itemView;

	private String userId;
	private User userProfile;
	private Map<Integer, Item> currentAuctions = new HashMap<>();
	
	private ViewRequest activeRequest = new ViewRequest();

	public static void main(String[] args) {
		final AuctionSystemClient system = new AuctionSystemClient();
		
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
				} catch (UnknownHostException e) {
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

	public void init() {
		setUpLogin();
		setUpAuctions();

		// Frame options
		this.pack();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(new Dimension(640, 480));
	}
	
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

		// Button handlers
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
					.addComponent(submit)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(register)
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
					.addComponent(submit)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(cancel)
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

	private void setUpAuctions() {
		setUpAuctionList();
		setUpItemView();
		
		JPanel sidebar = new JPanel();
		JPanel auctionFilters = new JPanel();
		JScrollPane auctionScroller = new JScrollPane(
				auctionList,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		sidebar.setLayout(new BorderLayout());
		sidebar.add(auctionFilters, BorderLayout.NORTH);
		sidebar.add(auctionScroller, BorderLayout.CENTER);
		
		auctions = new JPanel();
		auctions.setLayout(new BorderLayout());
		auctions.add(sidebar, BorderLayout.WEST);
		auctions.add(itemView, BorderLayout.CENTER);
		
		/*
		 * Create filters menu
		 */
		JLabel filterBy = new JLabel("Filter auctions");
		final PlaceholderComboBox category = new PlaceholderComboBox("category");
		for (Category c : Category.values()) {
			category.addItem(c);
		}
		final JTextField seller = new PlaceholderTextField("seller");
		final JTextField itemid = new PlaceholderTextField("item id");

		class FilterChangeListener implements DocumentListener, ActionListener {
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
			
			private void update() {
				synchronized (activeRequest) {
					if (category.getRealSelectedItem() != null) {
						activeRequest.setCategory((Category) category.getRealSelectedItem());
					} else {
						activeRequest.setCategory(null);
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
		
		GroupLayout auctionFilterLayout = new GroupLayout(auctionFilters);
		auctionFilterLayout.setHorizontalGroup(auctionFilterLayout.createSequentialGroup()
			.addContainerGap()
			.addGroup(auctionFilterLayout.createParallelGroup()
				.addComponent(filterBy)
				.addComponent(category)
				.addComponent(seller)
				.addComponent(itemid)
			)
			.addContainerGap()
		);
		auctionFilterLayout.setVerticalGroup(auctionFilterLayout.createSequentialGroup()
			.addContainerGap()
			.addComponent(filterBy)
			.addPreferredGap(ComponentPlacement.UNRELATED)
			.addComponent(category)
			.addPreferredGap(ComponentPlacement.RELATED)
			.addComponent(seller)
			.addPreferredGap(ComponentPlacement.RELATED)
			.addComponent(itemid)
			.addContainerGap()
		);
		auctionFilters.setLayout(auctionFilterLayout);
			
		/*GroupLayout auctionsLayout = new GroupLayout(auctions);
		auctionsLayout.setHorizontalGroup(auctionsLayout.createSequentialGroup()
			.addComponent(auctionScroller)
			.addComponent(itemView)
		);
		auctionsLayout.setVerticalGroup(auctionsLayout.createParallelGroup()
			.addComponent(auctionScroller, Short.MAX_VALUE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(itemView)
		);*/
	}
	
	private void setUpAuctionList() {
		auctionList = new JPanel();
		auctionList.setPreferredSize(new Dimension(200, 0));
	}

	private void setUpItemView() {
		itemView = new JPanel();
		itemView.setBackground(Color.WHITE);
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

	public void showItem(final Integer i) {
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
		itemView.add(panel);
		itemView.revalidate();
		itemView.repaint();
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
									ListingPanel l = new ListingPanel(currentAuctions.get(i));
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
	 */
	private void updateSession(Response re) {
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
		} catch (NotLoggedInException ex) {
			showLogin();
			Alerter.getHandler().warning("Session Timeout", ex.getMessage());
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
				updateSession(re);
				if (this.getContentPane() == login) {
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
		if (re != null) {
			updateSession(re);
			if (re.isSuccess()) {
				Alerter.getHandler().warning("Item Bid", "Your bid was successful.");
				updateAuctionList();
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
		if (re != null) {
			updateSession(re);
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
				updateSession(re);
				if (this.getContentPane() == register) {
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
				updateSession(re);
				Alerter.getHandler().warning("Profile Update Failure", re.getMessage());
			}
		}
	}
	
	/**
	 * Receives and handles a ViewResponse.
	 * 
	 * @param re
	 */
	public synchronized void handle(ViewResponse re) {
		if (re != null) {
			updateSession(re);
			if (re.isSuccess()) {
				currentAuctions = re.getItems();
			} else {
				Alerter.getHandler().warning("Auction Listing Failure", re.getMessage());
			}
		}
	}
}

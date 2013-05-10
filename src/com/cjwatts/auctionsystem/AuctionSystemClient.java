package com.cjwatts.auctionsystem;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.swing.*;
import javax.swing.LayoutStyle.ComponentPlacement;

import com.cjwatts.auctionsystem.alert.Alerter;
import com.cjwatts.auctionsystem.entity.Item;
import com.cjwatts.auctionsystem.entity.User;
import com.cjwatts.auctionsystem.gui.GUIAlert;
import com.cjwatts.auctionsystem.gui.ItemPanel;
import com.cjwatts.auctionsystem.gui.BidEvent;
import com.cjwatts.auctionsystem.gui.ItemPanelListener;
import com.cjwatts.auctionsystem.gui.ListingPanel;
import com.cjwatts.auctionsystem.io.Comms;
import com.cjwatts.auctionsystem.message.*;
import com.cjwatts.auctionsystem.security.PasswordHasher;

public class AuctionSystemClient extends JFrame {
	private static final long serialVersionUID = 1L;

	// Menu screen content panes
	private JPanel login;
	private JPanel auctions;
	private JPanel auctionList;
	private JPanel itemView;

	private String userId;
	private User userProfile;
	private ArrayList<Item> currentAuctions = new ArrayList<>();

	// Used for saving the previous window state after timeouts
	private Container previousWindow;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				AuctionSystemClient frame = new AuctionSystemClient();
				GUIAlert alerts = new GUIAlert(frame);
				Alerter.setHandler(alerts);
				frame.init();
				frame.showLogin();
				frame.setVisible(true);
			}
		});
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
		login = new JPanel();

		// Controls
		final JTextField username = new JTextField();
		final JPasswordField password = new JPasswordField();
		JButton submit = new JButton("Login");
		JButton register = new JButton("Register");

		JPanel centreBox = new JPanel();
		centreBox.setPreferredSize(new Dimension(200, 100));
		centreBox.setMinimumSize(new Dimension(200, 100));
		centreBox.setMaximumSize(new Dimension(200, 100));

		// Button handlers
		submit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Send request with username and password
				AuthenticateRequest rq = new AuthenticateRequest();

				userId = username.getText();
				char[] pw = password.getPassword();
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
				RegisterRequest rq = new RegisterRequest();
				
				userId = username.getText();
				char[] pw = password.getPassword();
				byte[] hash;

				PasswordHasher ph = new PasswordHasher();
				hash = ph.preHash(userId, new String(pw));
				// Clear password array for extra security
				Arrays.fill(pw, '\0');
				rq.setSenderId(userId);
				rq.setPassword(hash);
				
				User user = new User();
				// TODO: First and last name fields
				user.setFirstName("Undefined");
				user.setLastName("Undefined");
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

	private void setUpAuctions() {
		setUpAuctionList();
		setUpItemView();
		
		auctions = new JPanel();
		auctions.setLayout(new BorderLayout());
		
		auctions.add(auctionList, BorderLayout.WEST);
		auctions.add(itemView, BorderLayout.EAST);
	}
	
	private void setUpAuctionList() {
		auctionList = new JPanel();
		auctionList.setBackground(Color.BLACK);
		
		// Content
		JPanel items = new JPanel();
		items.setLayout(new GridLayout(0, 1));
		
		JScrollPane scroller = new JScrollPane(
				items,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				
		Item test1 = new Item();
		test1.setCategory("food");
		test1.setDescription("Have you ever wanted chicken? Now you can have it whenever you want!");
		test1.setStart(new Timestamp(new Date().getTime()));
		test1.setEnd(new Timestamp(1402862400));
		test1.setTitle("Chickenator");
		currentAuctions.add(test1);
		
		Item test2 = new Item();
		test2.setCategory("food");
		test2.setDescription("Have you ever wanted bacon? Now you can have it whenever you want!");
		test2.setStart(new Timestamp(new Date().getTime()));
		test2.setEnd(new Timestamp(1402862400));
		test2.setTitle("Baconator");
		currentAuctions.add(test2);
		
		// Click handlers
		for (final Item i : currentAuctions) {
			ListingPanel l = new ListingPanel(i);
			l.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					showItem(i);
				}
			});
			items.add(l);
		}
		
		auctionList.add(scroller);
	}

	private void setUpItemView() {
		itemView = new JPanel();
		itemView.setBackground(Color.RED);
		
		Item test1 = new Item();
		test1.setCategory("food");
		test1.setDescription("Have you ever wanted chicken? Now you can have it whenever you want!");
		test1.setStart(new Timestamp(new Date().getTime()));
		test1.setEnd(new Timestamp(1402862400));
		test1.setTitle("Chickenator");
		
		showItem(test1);
	}

	/**
	 * Swap the current content pane with the new container.
	 * Keeps track of previous window also.
	 * @param contentPane
	 */
	private void show(Container contentPane) {
		previousWindow = this.getContentPane();
		this.setContentPane(contentPane);
		this.revalidate();
	}
	
	public void showLogin() {
		show(login);
	}

	public void showAuctions() {
		show(auctions);
	}

	public void showItem(Item i) {
		Item retrieved = i;
		ItemPanel panel = new ItemPanel(retrieved);
		panel.addItemPanelListener(new ItemPanelListener() {
			@Override
			public void bidSubmitted(BidEvent e) {
				BidRequest rq = new BidRequest();
				rq.setSenderId(userId);
				rq.setSessionToken(userProfile.getSessionToken());
				//rq.setItemId(itemId);
				rq.setBid(e.getBid());

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
		});
		itemView.removeAll();
		itemView.add(new ItemPanel(retrieved));
		itemView.revalidate();
	}

	/**
	 * Check that a new token has been returned
	 * and update the session and user profile accordingly.
	 * 
	 * @param re Response from server after last transaction
	 */
	private void updateSession(Response re) {
		byte[] token = re.getSessionToken();
		// This should really use exceptions, but I think it's a bit of a waste.
		if (token != null) {
			if (userProfile != null) {
				if (!Arrays.equals(token, userProfile.getSessionToken())) {
					userProfile.setSessionToken(token);
				} else {
					timeout();
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
				timeout();
			}
		} else {
			timeout();
		}
	}
	
	/**
	 * Redirects the user back to login displaying a timeout message
	 */
	private void timeout() {
		previousWindow = this.getContentPane();
		show(login);
		Alerter.getHandler().warning("Session Timeout", "Your session has expired. Please log in again to resume.");
	}

	/**
	 * Receives and handles an AuthenticateResponse.
	 * 
	 * @param re
	 */
	public void handle(AuthenticateResponse re) {
		if (re != null) {
			if (re.isSuccess()) {
				if (this.getContentPane() == login) {
					show(auctions);
				}
				updateSession(re);
			} else {
				userId = null;
				show(login);
				Alerter.getHandler().warning("Authentication Failure", re.getMessage());
			}
		}
	}

	/**
	 * Receives and handles a BidResponse.
	 * 
	 * @param re
	 */
	public void handle(BidResponse re) {
		if (re != null) {
			
		}
	}

	/**
	 * Receives and handles a CreateItemResponse.
	 * 
	 * @param re
	 */
	public void handle(CreateItemResponse re) {
		if (re != null) {
			
		}
	}

	/**
	 * Receives and handles a RegisterResponse.
	 * 
	 * @param re
	 */
	public void handle(RegisterResponse re) {
		if (re != null) {
			if (re.isSuccess()) {
				if (this.getContentPane() == login) {
					show(auctions);
				}
				updateSession(re);
			} else {
				userId = null;
				show(login);
				Alerter.getHandler().warning("Registration Failure", re.getMessage());
			}
		}
	}

	/**
	 * Receives and handles an UpdateProfileResponse.
	 * 
	 * @param re
	 */
	public void handle(UpdateProfileResponse re) {
		if (re != null) {
			if (re.isSuccess()) {
				this.userProfile = re.getProfile();
				updateSession(re);
			} else {
				Alerter.getHandler().warning("Profile Update Failure", re.getMessage());
			}
			updateSession(re);
		}
	}
	
	/**
	 * Receives and handles a ViewResponse.
	 * 
	 * @param re
	 */
	public void handle(ViewResponse re) {
		if (re != null) {
			
		}
	}
}

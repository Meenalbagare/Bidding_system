package clientGui;

/*
 * this is action interface and contain subscribed item details table, symbol list, section to give symbol and get details related that symbol,
 * 	bidding section 
 * Constructor get input as Socket and user name of the client
 */

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import client.SubProcessRunnable;
import client.SubscribeList;
import clientCore.Item;
import clientModel.ItemModel;
import clientModel.SubscriberListModel;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class ActionGUI extends JFrame {

	private JPanel contentPane;
	private Socket cSoc, subSoc;
	private String uName;
	private SubscriberListModel submod = null;
	private SubscriberListModel nsymMod = null; // to new symbol table
	private ItemModel itmmodl = null;
	private JTextField symtextField;
	
	private SubscribeList sublst;
	private List<Item> itmLst = null;
	private List<String> symboles = null; // create list for symbols
	private List<String> newSymbol = new ArrayList<>(); // new symbol list
	private JTable table3;
	private JTable table;
	private JTable table_1;
	private JTextField bidSymtextField;
	private JTextField pricetextField;
	private static final String url = "jdbc:postgresql://localhost:5432/test";
	private static final String user = "postgres";
	private static final String password = "1234";	
	// if this is true a bid can be done, else bidding time is finished
	private static boolean biddStat = true; 	
	// to store and map CSV file data and mapping them with symbol
	// Hash table used to thread safety
	private Map<String, String> symPwdMap = new HashMap<>();
	  private Map<String, Float> symBidMap = new HashMap<>();
	  private Map<String, Integer> symFunMap = new HashMap<>();
	  private Map<String, String> symCusMap = new HashMap<>(); // May not be used in this version
	  private Map<String, String> symhBidTim = new HashMap<>(); //Symbol with highest Bid Time
	
	
	private int guiSynCntrl; // avoid interruption when same time call multiple methods
	private JTable table_2;

	/**
	 * Create the frame.
	 */
	public ActionGUI(Socket soc, String uname, Socket sSoc) {
		
		this.cSoc = soc; // client server socket
		this.uName = uname; // user ID
		this.subSoc = sSoc; // publisher subscriber socket
		setTitle("Client Account - "+ uName);
		
		sublst = new SubscribeList(); // create object of SubscribeList class
		
		//createITMTablemodel(cSoc);
		
		createSymbolTable(cSoc); // receive symbol list and create symbol model
		
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 664, 544);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(485, 192, 153, 272);
		contentPane.add(scrollPane_2);
		
		table_1 = new JTable();
		scrollPane_2.setViewportView(table_1);
		
		//display symbol table
		setSymTable(submod);
		
		// subscribed item
		JButton SubLstbtnNewButton = new JButton("Subscribe Item");
		SubLstbtnNewButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		SubLstbtnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.printf("\n%s : -------------------Subscribe Item---------------------\n", time());
				if(guiSynCntrl == 0) {
					guiSynCntrl = 1; // block access to the other actions
					int row1 = table_1.getSelectedRow(); // get selected row number
					
					if(row1 < 0 ) { 
						JOptionPane.showMessageDialog(ActionGUI.this, "You must select a Symbol", "Error",
								JOptionPane.ERROR_MESSAGE);
						guiSynCntrl = 0;
						return;
					}
					
					String selSym1 = (String) table_1.getValueAt(row1, SubscriberListModel.ITEM); // get selected symbol
					System.out.printf("%s : Client : [%s] symbol selected to subscribe\n", time(), selSym1);
					int subSus = 0;
					
					try {
						subSus = sublst.SubscribeItem(cSoc, uName, selSym1); // to subscribe 
						
						//refresh table with subscribed Item
						if(subSus == 0) { 
							
							createITMTablemodel(cSoc);
							
							JOptionPane.showMessageDialog(ActionGUI.this, "Subscribed Successfully : " + selSym1, "Subscribed",
									JOptionPane.INFORMATION_MESSAGE);
							guiSynCntrl = 0; // set access for other process
							return;
						}
						else {
							JOptionPane.showMessageDialog(ActionGUI.this, "Already Subscribed : " + selSym1, "Subscribed",
									JOptionPane.ERROR_MESSAGE);
							guiSynCntrl = 0; // set access for other process
							return;
						}	
					}
					// if connection loss happen
					catch(Exception ex) {
						System.out.printf("%s : Connection failed...\n", time());
						JOptionPane.showMessageDialog(ActionGUI.this, "Connection was lost. Please log-in again.", "Connection Failed", JOptionPane.ERROR_MESSAGE);
						guiSynCntrl = 0;
					}	
				}	
			}
		});
		SubLstbtnNewButton.setBounds(505, 471, 122, 23);
		contentPane.add(SubLstbtnNewButton);
		
		symtextField = new JTextField();
		symtextField.setBounds(98, 290, 122, 20);
		contentPane.add(symtextField);
		symtextField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Symbol :");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel.setBounds(31, 293, 68, 14);
		contentPane.add(lblNewLabel);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 318, 457, 52);
		contentPane.add(scrollPane_1);
		
		table = new JTable();
		scrollPane_1.setViewportView(table);
		
		// check details of given symbol
		JButton bidbtnNewButton = new JButton("Check Item Details");
		bidbtnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.printf("\n%s : -------------------Check Item Details---------------------\n", time());
				if(guiSynCntrl == 0) {
					if(!symtextField.getText().isBlank()) {
						guiSynCntrl = 1;
						List<Item> chkItmLst = new ArrayList<>(); // to store given item
						
						try {
							Item tmpItm = sublst.checkItem(cSoc, symtextField.getText()); // get given Item Details
							
							// create table if valid symbol
							if(!tmpItm.getSym().equals("Empty")) {
								chkItmLst.add(tmpItm);
								ItemModel chkItmmodl = new ItemModel(chkItmLst);
								table.setModel(chkItmmodl);
							}
							else {
								JOptionPane.showMessageDialog(ActionGUI.this, "Wrong Symbol : " + symtextField.getText(), "Wrong Symbol", JOptionPane.ERROR_MESSAGE);
							}
						}
						// if connection loss happen
						catch(Exception ex) {
							JOptionPane.showMessageDialog(ActionGUI.this, "Connection was lost. Please log-in again.", "Connection Failed", JOptionPane.ERROR_MESSAGE);
							guiSynCntrl = 0;
						}
					}
					else {
						JOptionPane.showMessageDialog(ActionGUI.this, "Plese Enter a Symbol", "Error", JOptionPane.ERROR_MESSAGE);
					}
					guiSynCntrl = 0;
				}
			}
		});
		bidbtnNewButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		bidbtnNewButton.setBounds(261, 289, 176, 23);
		contentPane.add(bidbtnNewButton);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 28, 457, 207);
		contentPane.add(scrollPane);
		
		table3 = new JTable();
		scrollPane.setViewportView(table3);
		
		//close
		JButton btnNewButton = new JButton("Close");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.printf("\n%s : -------------------Close---------------------\n", time());
				try {
					sublst.closeConClient(cSoc);
				}
				catch(Exception ex) {
					System.out.printf("%s : *** Server was Closed Unexpectedly ***\n", time());
					setVisible(false);
					dispose();
				}
				
				setVisible(false);
				dispose();
				System.out.printf("%s : Client Account Interface was closed\n", time());
			}
		});
		btnNewButton.setBounds(10, 471, 89, 23);
		contentPane.add(btnNewButton);
		
		JLabel lblNewLabel_1 = new JLabel("Subscribed Items");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_1.setBounds(10, 3, 307, 14);
		contentPane.add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Check Item Price And Profit");
		lblNewLabel_2.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_2.setBounds(10, 265, 272, 14);
		contentPane.add(lblNewLabel_2);
		
		JLabel lblNewLabel_3 = new JLabel("Bid On Item");
		lblNewLabel_3.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_3.setBounds(10, 397, 176, 14);
		contentPane.add(lblNewLabel_3);
		
		JLabel lblNewLabel_4 = new JLabel("Symbol :");
		lblNewLabel_4.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_4.setBounds(31, 422, 68, 14);
		contentPane.add(lblNewLabel_4);
		
		bidSymtextField = new JTextField();
		bidSymtextField.setBounds(100, 416, 89, 20);
		contentPane.add(bidSymtextField);
		bidSymtextField.setColumns(10);
		
		JLabel lblNewLabel_5 = new JLabel("Price :");
		lblNewLabel_5.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_5.setBounds(214, 422, 34, 14);
		contentPane.add(lblNewLabel_5);
		
		pricetextField = new JTextField();
		pricetextField.setBounds(261, 416, 86, 20);
		contentPane.add(pricetextField);
		pricetextField.setColumns(10);
		
		// make a new bid
		JButton btnNewButton_1 = new JButton("Bid");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.printf("\n%s : -------------------Bid---------------------\n", time());
				if(guiSynCntrl == 0) {
					if(!(bidSymtextField.getText().isBlank() || pricetextField.getText().isBlank())) {
						guiSynCntrl = 1;
						
						try {
							int bidflg = sublst.bidOnItem(cSoc, uName, bidSymtextField.getText(), pricetextField.getText());
							
							if(bidflg == 1) {
								System.out.printf("%s : Server : [1] Bid is less than current max bid\n", time());
								JOptionPane.showMessageDialog(ActionGUI.this, bidSymtextField.getText() + " Your Bid Is less then Current max bid", "Bidding Failed", JOptionPane.ERROR_MESSAGE);
							}
							else if(bidflg == 2) {
								System.out.printf("%s : Server : [2] Bidding time is over\n", time());
								JOptionPane.showMessageDialog(ActionGUI.this, bidSymtextField.getText() + " Bidding Time is over. You cannot bid anymore", "Bidding Time Out", JOptionPane.ERROR_MESSAGE);
							}
							else if(bidflg == 3) {
								System.out.printf("%s : Server : [3] Bidding Successful\n", time());
								JOptionPane.showMessageDialog(ActionGUI.this, "Bidding Successful : " + bidSymtextField.getText(), "Success..", JOptionPane.INFORMATION_MESSAGE);
							}
						}
						// if connection loss happen
						catch(Exception ex) {
							System.out.printf("%s : Connection Failed\n", time());
							JOptionPane.showMessageDialog(ActionGUI.this, "Connection was lost. Please log-in again.", "Connection Failed", JOptionPane.ERROR_MESSAGE);
							guiSynCntrl = 0;
						}
					}
					else {
						JOptionPane.showMessageDialog(ActionGUI.this, "Plese Enter a Symbol and Price", "Error", JOptionPane.ERROR_MESSAGE);
					}
					guiSynCntrl = 0;
				}
			}
		});
		btnNewButton_1.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnNewButton_1.setBounds(378, 418, 89, 23);
		contentPane.add(btnNewButton_1);
		
		JLabel lblNewLabel_6 = new JLabel("New Items");
		lblNewLabel_6.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_6.setBounds(538, 3, 89, 14);
		contentPane.add(lblNewLabel_6);
		
		JScrollPane scrollPane_3 = new JScrollPane();
		scrollPane_3.setBounds(488, 28, 150, 114);
		contentPane.add(scrollPane_3);
		
		table_2 = new JTable();
		scrollPane_3.setViewportView(table_2);
		
		JLabel lblNewLabel_7 = new JLabel("Items");
		lblNewLabel_7.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_7.setBounds(542, 167, 46, 14);
		contentPane.add(lblNewLabel_7);
		
		// start subscriber process
		SubProcessRunnable spro = new SubProcessRunnable(subSoc,uName,ActionGUI.this);
		Thread subTr = new Thread(spro);
		subTr.start();
		
	}
	
	private void createSymbolTable(Socket pSoc) {
		
		symboles = loadDataFromDatabase();
		Collections.sort(symboles); // sort symbol list
		submod = new SubscriberListModel(symboles); //create model of symbols for create table
		
	}
	
	public synchronized void createITMTablemodel(Socket pSoc) {
		loadDataFromDatabase();
		itmLst = sublst.getAlrdySubItemWithDitail(pSoc, uName); // get already subscribed Symbol list with it's details from server
		itmmodl = new ItemModel(itmLst); // set model
		setSubTable(itmmodl);
	}
	
	// update table for price update
	public void createPUpdateTable(String sym, String npr) {
		loadDataFromDatabase();
		List<Item> itLst = sublst.getPriceUpdateItemWithD(sym, npr);
		ItemModel tl = new ItemModel(itLst);
		setSubTable(tl);
	}
	
	// update table for profit update
	public void createProUpdateTable(String sym, String npro) {
		loadDataFromDatabase();
		List<Item> itLst = sublst.getProUpdateItemWithD(sym, npro);
		ItemModel tl = new ItemModel(itLst);
		setSubTable(tl);
	}
	
	// update symbol table
	public void createNewSymTable(String sym) {
		loadDataFromDatabase();
		symboles = sublst.getUpdateStringList(sym);
		submod = new SubscriberListModel(symboles); //create model of symbols to create table
		setSymTable(submod);
	}
	
	// create new symbol and refresh
	public void newSymbolTable(String symbol) {
		loadDataFromDatabase();
		newSymbol.add(symbol);
		nsymMod = new SubscriberListModel(newSymbol);
		setNewSymTable(nsymMod);
	}
	
	// set subscribed item table
	private synchronized void setSubTable(ItemModel itm) {
		table3.setModel(itm);
	}
	
	// set symbol table
	private synchronized void setSymTable(SubscriberListModel ssubmod) {
		table_1.setModel(ssubmod);	
	}
	
	// create new symbol table
	private synchronized void setNewSymTable(SubscriberListModel ssubmod) {
		table_2.setModel(ssubmod);
	}
	
	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	
	}
	private List<String> loadDataFromDatabase() {
		List<String> symbolList = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT \"Symbol\" FROM stocks";
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String symbol = rs.getString("Symbol");
                symbolList.add(symbol);
                
            }
        } catch (Exception e) {
            System.out.printf("%s : Cannot read the csv file \n", time());
        }
        return symbolList;
    }
	
	
}

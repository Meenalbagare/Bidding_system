package companyGUI;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import company.ComConn;
import companySupMethod.CompanySupMethods;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.event.ActionEvent;
import company.Connection;
public class CompanyStartGUI extends JFrame {
	
	private String sIp; //server iP
	private int sPort; //server port
	private int subPort; //subscriber Port
//	private ComConn conCon = null; //to create company side socket
	private Socket rsSoc = null;
	private CompanySupMethods comSupO = new CompanySupMethods();

	private JPanel contentPane;
	private JTextField iptextField;
	private JTextField porttextField;
	private JTextField subPorttextField;
	private Connection conCon = null;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CompanyStartGUI frame = new CompanyStartGUI(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public CompanyStartGUI(Connection conCon) {
		setTitle("Company Start GUI");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 379, 197);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Server IP Address :");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel.setBounds(10, 28, 121, 14);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Client Server Port  :");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_1.setBounds(10, 62, 121, 14);
		contentPane.add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Subscriber Port      :");
		lblNewLabel_2.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_2.setBounds(10, 97, 110, 14);
		contentPane.add(lblNewLabel_2);
		
		iptextField = new JTextField();
		iptextField.setBounds(141, 25, 201, 20);
		contentPane.add(iptextField);
		iptextField.setColumns(10);
		
		porttextField = new JTextField();
		porttextField.setBounds(141, 59, 201, 20);
		contentPane.add(porttextField);
		porttextField.setColumns(10);
		
		subPorttextField = new JTextField();
		subPorttextField.setBounds(141, 94, 201, 20);
		contentPane.add(subPorttextField);
		subPorttextField.setColumns(10);
		
		JButton okbtnNewButton = new JButton("OK");
		okbtnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ComConn conCon = new ComConn();

				// check Empty Fields
				if(!(iptextField.getText().isBlank() || porttextField.getText().isBlank() || subPorttextField.getText().isBlank())) {
					//check, Port numbers are valid Integer
					if(comSupO.isInteger(porttextField.getText()) && comSupO.isInteger(subPorttextField.getText())) {
						
						int response = JOptionPane.showConfirmDialog(
								CompanyStartGUI.this, "Check again whether the enterd port numbers are correct"
										, "Port Confirm", 
										JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						
						if (response != JOptionPane.YES_OPTION) {
							return;
						}
						
						try {
							sIp = iptextField.getText(); // get IP
							sPort = Integer.parseInt(porttextField.getText()); // get Client server Port
							subPort = Integer.parseInt(subPorttextField.getText()); // get Publisher Subscriber Port
							
							try {
								conCon.connect(sIp, sPort); // to get Connection to Client Server Port 
							}
							catch(Exception ex) {
								System.out.printf("%s : Cannot connect to the server\n", time());
							}
							
							rsSoc = conCon.getSocket(); // get client server socket
							
							CompanyLogin clo = new CompanyLogin(rsSoc, subPort,sIp);
							System.out.printf("%s : Company Login GUI opened\n", time());
							clo.setVisible(true);
							setVisible(false);
							dispose();
						}
						catch(Exception ex) {
							System.out.printf("%s : Connection Error!...\n", time());
							JOptionPane.showMessageDialog(CompanyStartGUI.this, "Connection Error : " + sIp + ":" + sPort, "Connection Error", JOptionPane.ERROR_MESSAGE);
						}
						
					}
					else {
						System.out.printf("%s : Port numbers are invalid\n", time());
						JOptionPane.showMessageDialog(CompanyStartGUI.this, "Enter Valid Port", "Port Error", JOptionPane.ERROR_MESSAGE); // Port Error Message
					}
					
				}
				else {
					System.out.printf("%s : IP and port numbers are empty\n", time());
					JOptionPane.showMessageDialog(CompanyStartGUI.this, "Enter IP and Port Number", "Error", JOptionPane.ERROR_MESSAGE); // Empty Field Error
				}
			}
		});
		okbtnNewButton.setFont(new Font("Tahoma", Font.BOLD, 12));
		okbtnNewButton.setBounds(253, 125, 89, 23);
		contentPane.add(okbtnNewButton);
	}
	
	// get current time
	public String time() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}	
}
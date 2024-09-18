/*
Copyright (c) Rob Parker 2024

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at:

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 Contributors:
   Rob Parker - Initial Contribution
*/
package swiftdemoapp;

import java.util.Random;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class is used to create or parse a SWIFT MT103 message. It contains
 * various functions that generate or construct the necessary fields for a MT103
 * message.
 */
public class SWIFTcoder {
	private String sendBank;
	private String sendAccount;
	private String sendName;
	private String destBank;
	private String destAccount;
	private String destName;
	private int ammount;
	private Currency currency;
	private String sendBranch;
	private String destBranch;
	private String session;
	private String seq;

	private String reference3;
	private String transactionRefence;

	/**
	 * Generates a random string of a number of characters. Characters can be A-Z
	 * and 0-9.
	 * 
	 * @param charcters number of characters to generate.
	 * @return The random string.
	 */
	public static String generateReference(int charcters) {
		String validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < charcters; i++) {
			char randomNumber = validChars.charAt(random.nextInt(validChars.length()));
			sb.append(randomNumber);
		}
		return sb.toString();
	}

	public SWIFTcoder(String sendBank, String sendAccount, String sendName, String destBank, String destAccount,
			String destName, int ammount, Currency currency, String sendBranch, String destBranch, int session,
			int seq) {
		this.sendBank = sendBank;
		this.sendAccount = sendAccount;
		this.sendName = sendName;
		this.destBank = destBank;
		this.destAccount = destAccount;
		this.destName = destName;
		this.ammount = ammount;
		this.currency = currency;
		this.sendBranch = sendBranch;
		this.destBranch = destBranch;

		this.session = intToStrPlace(session, 4);
		this.seq = intToStrPlace(seq, 6);
		this.reference3 = generateReference(16);
		this.transactionRefence = sendName.substring(0, 3).toUpperCase() + "TO" + destName.substring(0, 3).toUpperCase()
				+ seq;
	}

	/**
	 * Converts a given number to a string representation ensuring it has a minimum
	 * of the given number of places.
	 * 
	 * @param in     The number to convert to a string.
	 * @param places THe minimum number of places the string must have. For example.
	 *               a value of '4' will result in a 4 digit number: 0010, 1000,
	 *               0983.
	 * @return The number string.
	 */
	private String intToStrPlace(int in, int places) {
		String current = String.valueOf(in);
		int diff = places - current.length();
		if (diff < 0) {
			current = current.substring(1, current.length());
		} else {
			while (diff != 0) {
				current = "0" + current;
				diff--;
			}
		}
		return current;
	}

	/**
	 * Converts a given string message into a SWIFTcoder object. Parses the message
	 * as a SWIFT MT103 message extracting the necessary fields.
	 * 
	 * @param message The message to parse.
	 * @throws Exception
	 */
	public SWIFTcoder(String message) throws Exception {

		String splitted[] = message.split("\r\n");
		// Header
		sendBank = splitted[0].substring(6, 14);
		sendBranch = splitted[0].substring(15, 18);
		session = splitted[0].substring(18, 22);
		seq = splitted[0].substring(22, 28);
		destBank = splitted[0].substring(36, 44);
		destBranch = splitted[0].substring(45, 48);
		reference3 = splitted[0].substring(72, 88);

		// User data
		transactionRefence = splitted[1].substring(4);

		currency = Currency.parse(splitted[3].substring(11, 14));
		ammount = Integer.parseInt(splitted[3].substring(14, splitted[3].indexOf(',', 14)));

		String accSplit[] = splitted[4].substring(6).split(" ");
		sendAccount = accSplit[0];
		sendName = accSplit[1];

		String accSplit2[] = splitted[5].substring(5).split(" ");
		destAccount = accSplit2[0];
		destName = accSplit2[1];
	}

	/**
	 * Converts this SWIFTcoder object into a String representation of the SWIFT
	 * MT103 message ready to be sent.
	 * 
	 * @return The string messsage to be sent.
	 * @throws NoSuchAlgorithmException
	 */
	public String getMessage() throws NoSuchAlgorithmException {
		// Header 1
		String swiftMessage = "{1:F01" + sendBank + "Z" + sendBranch + session + seq + "}";
		// Header 2
		swiftMessage += "{2:I103" + destBank + "X" + destBranch + "N1020}";
		// Header 3
		swiftMessage += "{3:{113:SEPA}{108:" + reference3 + "}}";
		// User data
		swiftMessage += "{4\r\n"; // splitted[0]
		// - Transaction
		swiftMessage += ":20:" + transactionRefence + "\r\n"; // splitted[1]

		// - Operation code
		swiftMessage += ":23B:CRED\r\n"; // splitted[2]

		// - Value: DATE (YYMMDD), Currency, Amount
		String amountS = String.valueOf(ammount);
		if (amountS.endsWith(".")) {
			amountS += "00";
		}

		amountS = amountS.replace('.', ',');
		swiftMessage += ":32A:" + getDate() + currency.getSwiftCode() + amountS + ",00\r\n"; // splitted[3]

		// - Sender Customer number and Name
		swiftMessage += ":50A:/" + sendAccount + " " + sendName + "\r\n"; // splitted[4]

		// - Receive Customer number and Name
		swiftMessage += ":59:/" + destAccount + " " + destName + "\r\n"; // splitted[5]

		// - Details of Remittance
		swiftMessage += ":70:INVOICE " + seq + "\r\n";// splitted[6]

		// - Details of charges
		swiftMessage += ":71A:SHA\r\n"; // splitted[7]
		// - End
		swiftMessage += "-}\r\n"; // splitted[8]
		// Footer
		String checksum = generateCheckSum(swiftMessage);
		swiftMessage += "{5:{CHK:" + checksum + "}}"; // splitted[9]

		return swiftMessage;
	}

	public String getSendBank() {
		return sendBank;
	}

	public void setSendBank(String sendBank) {
		this.sendBank = sendBank;
	}

	public String getSendAccount() {
		return sendAccount;
	}

	public void setSendAccount(String sendAccount) {
		this.sendAccount = sendAccount;
	}

	public String getSendName() {
		return sendName;
	}

	public void setSendName(String sendName) {
		this.sendName = sendName;
	}

	public String getDestBank() {
		return destBank;
	}

	public void setDestBank(String destBank) {
		this.destBank = destBank;
	}

	public String getDestAccount() {
		return destAccount;
	}

	public void setDestAccount(String destAccount) {
		this.destAccount = destAccount;
	}

	public String getDestName() {
		return destName;
	}

	public void setDestName(String destName) {
		this.destName = destName;
	}

	public int getAmmount() {
		return ammount;
	}

	public void setAmmount(int ammount) {
		this.ammount = ammount;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	// Assisted by WCA@IBM
	// Latest GenAI contribution: ibm/granite-20b-code-instruct-v2
	/**
	 * @return Returns a date string in the format YYMMDD
	 */
	public String getDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
		Date date = new Date();
		String formattedDate = formatter.format(date);
		return formattedDate;
	}

	// Assisted by WCA@IBM
	// Latest GenAI contribution: ibm/granite-20b-code-
	/**
	 * Generates a MD5 checksum of a given string.
	 * 
	 * @param in The string to create the checksum for.
	 * @return The MD5 checksum.
	 * @throws NoSuchAlgorithmException
	 */
	private String generateCheckSum(String in) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] digest = md.digest(in.getBytes());
		return toHexString(digest);
	}

	// Assisted by WCA@IBM
	// Latest GenAI contribution: ibm/granite-20b-code-
	/**
	 * Converts the given byte array into a Hex string representation.
	 * 
	 * @param bytes The byte array to convert
	 * @return The hex string.
	 */
	private static String toHexString(byte[] bytes) {
		StringBuilder builder = new StringBuilder();
		for (byte b : bytes) {
			builder.append(String.format("%02x", b));
		}
		return builder.toString();
	}

	/**
	 * Prints out this SWIFTcoder object in a human readable format.
	 */
	public void printMe() {
		String out = "sendBank[" + sendBank + "] " + "sendAccount[" + sendAccount + "] " + "sendName[" + sendName + "] "
				+ "destBank[" + destBank + "] " + "destAccount[" + destAccount + "] " + "destName[" + destName + "] "
				+ "ammount[" + ammount + "] " + "currency[" + currency + "] " + "sendBranch[" + sendBranch + "] "
				+ "destBranch[" + destBranch + "] " + "session[" + session + "] " + "seq[" + seq + "] " + "reference3["
				+ reference3 + "] " + "transactionRefence[" + transactionRefence + "]";
		System.out.println(out);
	}

	/**
	 * Prints out a summary of this SWIFTcoder object.
	 */
	public void summarize() {
		String out = sendBank + "/" + sendName + "/" + ammount + ",00" + currency.getSwiftCode() + "->" + destBank + "/"
				+ destName;
		System.out.println(out);
	}
}

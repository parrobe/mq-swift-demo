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

import java.util.ArrayList;
import java.util.Random;

public class Bank {
	private String name;
	private String SWIFTName;
	private ArrayList<Account> accounts;
	private Currency defaultCur;
	private String qName;
	private String branchCode;

	/**
	 * Returns the IBM MQ Queue associated with this bank.
	 * 
	 * @return Queue the bank will get SWIFT Messages from.
	 */
	public String getqName() {
		return qName;
	}

	/**
	 * Creates a Bank object. Generates a random branch code for the bank.
	 * 
	 * @param name      Name of bank
	 * @param swiftname the SWIFT name of the bank. Must be 8 characters.
	 * @param cur       The currency of the bank.
	 * @param qname     The Name of the IBM MQ queue this bank will monitor.
	 * @throws Exception
	 */
	public Bank(String name, String swiftname, Currency cur, String qname) throws Exception {
		this.name = name;
		this.SWIFTName = swiftname;
		if (swiftname.length() != 8) {
			throw new Exception("Swift name must be 8 characters for the BIC.");
		}
		this.defaultCur = cur;
		accounts = new ArrayList<Account>();
		this.qName = qname;
		branchCode = generateBranchCode();
	}

	/**
	 * Returns the branch code
	 * 
	 * @return The branch code for this bank
	 */
	public String getBranchCode() {
		return branchCode;
	}

	public String getName() {
		return name;
	}

	public String getSWIFTName() {
		return SWIFTName;
	}

	public Currency getDefaultCur() {
		return defaultCur;
	}

	/**
	 * Creates a new Account object for the given name with a default starting
	 * balance of 1000. Adds the account to the banks list of acounts
	 * 
	 * @param name Account holder name
	 */
	public void openAccount(String name) {
		openAccount(name, 1000);
	}

	/**
	 * Creates a new Account object for the given name with a given starting
	 * balance. Adds the account to the banks list of acounts
	 * 
	 * @param name            Account holder name
	 * @param startingBalance Starting balance.
	 */
	public boolean openAccount(String name, int startingBalance) {
		String accnum;

		if (startingBalance < 0)
			startingBalance = 0;

		if (name.length() == 0) {
			return false;
		}
		do {
			accnum = generateAccNum();
			for (Account a : accounts) {
				if (a.getNumber().equals((accnum))) {
					// yuck a dupe. Try again
					continue;
				}
			}
			// not a dupe!
		} while (false);

		Account a = new Account(name, accnum, startingBalance);
		accounts.add(a);
		return true;
	}

	/**
	 * Returns a random account from the Banks accounts.
	 * 
	 * @return An Account registered with this bank.
	 */
	public Account getRandomAccount() {
		Random random = new Random();
		int index = random.nextInt(accounts.size());
		return accounts.get(index);
	}

	/**
	 * Returns the Account object with the matching account number or null.
	 * 
	 * @param number account number to find
	 * @return The associated Account or null.
	 */
	public Account getAccountByAccountNumber(String number) {
		for (Account a : accounts) {
			if (a.getNumber().equals(number)) {
				return a;
			}
		}
		return null;
	}

	/**
	 * Returns the Account object with the matching account name or null.
	 * 
	 * @param name account name to find
	 * @return The associated Account or null.
	 */
	public ArrayList<Account> getAccountsByAccountName(String name) {
		ArrayList<Account> toreturn = new ArrayList<Account>();
		for (Account a : accounts) {
			if (a.getName().equals(name)) {
				toreturn.add(a);
			}
		}
		return toreturn;
	}

	// Assisted by WCA@IBM
	// Latest GenAI contribution: ibm/granite-20b-code-instruct-v2
	/**
	 * Generates a random account number with 20 characters.
	 * 
	 * @return The random account number
	 */
	public static String generateAccNum() {
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < 20; i++) {
			int randomNumber = random.nextInt(10);
			sb.append(randomNumber);
		}
		return sb.toString();
	}

	/**
	 * Generates a random branch code of 3 characters made from A-G characters.
	 * 
	 * @return The Branch code.
	 */
	public static String generateBranchCode() {
		char validChar[] = { 'A', 'B', 'C', 'D', 'E', 'F', 'G' };
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < 3; i++) {
			char randomNumber = validChar[random.nextInt(validChar.length)];
			sb.append(randomNumber);
		}
		return sb.toString();
	}

	/**
	 * Prints a summary of the bank and accounts registered with it.
	 */
	public void printMe() {
		System.out.println("-- Start Bank --");
		System.out.println("BANK: " + name);
		System.out.println("SWIFT: " + SWIFTName);
		System.out.println("QNAME: " + qName);
		System.out.println("CURRENCY: " + defaultCur.getSwiftCode());
		System.out.println("Accounts: " + accounts.size());
		for (int i = 0; i < accounts.size(); i++) {
			System.out.print("  Account " + i + ": ");
			accounts.get(i).printMe();
		}
		System.out.println("-- End Bank --");
	}
}

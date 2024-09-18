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
import java.util.Scanner;

/**
 * This class is the main entry point for this program. It handles setting up
 * the necessary objects and threads before starting.
 */
public class Main {

	public static void main(String[] args) {
		Main m = new Main();
		try {
			m.go();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Main execution function. This function will do the following:
	 * 1. Create 3 banks with 4 accounts each.
	 * 2. Create the MoneyReceivers objects for each bank.
	 * 3. Create the MoneySender objects for each bank.
	 * 4. Register each bank with eachother.
	 * 5. Print a summary of all the banks.
	 * 6. Start all of the MoneyReceivers and MoneySender threads.
	 * 7. Pause waiting for a enter key to signal a stop.
	 * 8. Request all threads started end gracefully.
	 * 9. Wait until all threads have ended.
	 * 10. Print a summary of all the banks.
	 * 
	 * This functionality uses harcoded values for the queue manager, queues and
	 * bank names. In the future these should be changed to be read from a
	 * configuration file.
	 * The program assumes a queue manager locally called "QM1" with a channel of
	 * "IN" and port of "1414".
	 * The program creates 3 banks connecting to the queues:
	 * - BankOfRob -> BANKROB.Q
	 * - BankOfGraham -> BANKGRA.Q
	 * - BankOfNick -> BANKNICK.Q
	 * 
	 * @throws Exception
	 */
	public void go() throws Exception {
		ArrayList<Bank> banks = new ArrayList<Bank>();
		ArrayList<MoneyHandlers> threadsToMonitor = new ArrayList<MoneyHandlers>();
		// first bank
		Bank b1 = new Bank("BankOfRob", "BANKROBE", Currency.GBP, "BANKROB.Q");
		b1.openAccount("Rob Parker");
		b1.openAccount("Jimbo Blooms");
		b1.openAccount("Dwayne Johnson");
		b1.openAccount("Richard Liesen");
		banks.add(b1);

		// second bank
		Bank b2 = new Bank("BankOfGraham", "BANKGRAH", Currency.GBP, "BANKGRA.Q");
		b2.openAccount("Harry Houdini");
		b2.openAccount("Margret Allens");
		b2.openAccount("Alice Baker");
		b2.openAccount("Sherlock Holmes");
		banks.add(b2);

		// third bank
		Bank b3 = new Bank("BankOfNick", "BANKNICK", Currency.GBP, "BANKNICK.Q");
		b3.openAccount("David Ware");
		b3.openAccount("Amanda Maidstone");
		b3.openAccount("Paul Norfolk");
		b3.openAccount("Charlie Chesire");
		banks.add(b3);

		// Create the receivers for each bank
		for (Bank b : banks) {
			threadsToMonitor.add(createReceiverForBank(b));
		}

		// Create the senders for each bank
		for (int i = 0; i < banks.size(); i++) {
			MQI m = new MQI("QM1", "localhost", 1414, "IN");
			m.createConnection();
			MoneySender ms = new MoneySender(m, banks.get(i));
			for (int i2 = 0; i2 < banks.size(); i2++) {
				if (i2 == i) { // Don't add ourselves
					continue;
				}
				ms.addOtherBank(banks.get(i2));
			}

			threadsToMonitor.add(ms);
		}

		// debug
		for (Bank b : banks) {
			b.printMe();
		}

		// ok we are ready. Start all the threads!
		System.out.println("Starting all threads");
		for (MoneyHandlers mh : threadsToMonitor) {
			Thread t = new Thread(mh);
			t.start();
		}

		// Now we wait until enter key pressed.
		// Assisted by WCA@IBM
		// Latest GenAI contribution: ibm/granite-20b-code-instruct-v2
		Scanner scanner = new Scanner(System.in);
		System.out.println("Press ENTER to continue...");
		scanner.nextLine();
		scanner.close();

		System.out.println("Ending all threads");
		for (MoneyHandlers mh : threadsToMonitor) {
			mh.signalStop();
		}
		for (MoneyHandlers mh : threadsToMonitor) {
			while (mh.isActive())
				;
		}
		System.out.println("All threads closed. Stopping.");
		System.out.println("Final stats");
		for (Bank b : banks) {
			b.printMe();
		}
	}

	/**
	 * Creates a MQI object for a bank and then creates a MoneyReceiver for that
	 * bank.
	 * 
	 * @param b Bank to create the MoneyReceiver object for.
	 * @return The MoneyReceiver object.
	 * @throws Exception
	 */
	private MoneyReceiver createReceiverForBank(Bank b) throws Exception {
		MQI m = new MQI("QM1", "localhost", 1414, "IN");
		m.createConnection();
		MoneyReceiver mr = new MoneyReceiver(m, b);
		return mr;
	}

	private Bank createBankFromConfig() {
		// TODO banks from a config file instead of hardcoded
		return null;
	}
}

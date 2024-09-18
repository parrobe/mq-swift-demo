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

/**
 * This class handles simulating money being sent to a bank from another bank.
 */
public class MoneyReceiver implements MoneyHandlers {

	private MQI mqi;
	private Bank myBank;

	private boolean stop = false;
	private boolean active = false;

	/**
	 * Creates a MoneyReceiver supplying a MQI object to use for communicating to MQ
	 * and the Bank object that it is for.
	 * 
	 * @param mqi    The object to use for communicating with IBM MQ.
	 * @param myBank The bank this MoneyReceiver will be operating on.
	 * @throws Exception
	 */
	public MoneyReceiver(MQI mqi, Bank myBank) {
		this.mqi = mqi;
		this.myBank = myBank;
	}

	@Override
	public void signalStop() {
		stop = true;
	}

	/**
	 * The main loop for this logic, will be ran in a thread.
	 * This loop will run until it is signalled to stop or encounters too many
	 * errors.
	 * 
	 * In a loop it will:
	 * 1. Consume a message from its bank queue. If no message arrives within a
	 * timeout it will try again.
	 * 2. Convert the given message into a SWIFTcoder object.
	 * 3. Extract the receiving account number from the SWIFTcoder object and find
	 * that account within the bank.
	 * 4. Deposit the amount of money given to that account based off the SWIFT
	 * message values.
	 */
	@Override
	public void run() {
		int failC = 0;
		active = true;
		String q = myBank.getqName();
		System.out.println("Receiving thread for bank " + myBank.getSWIFTName() + " now active.");
		while (stop == false) {
			try {
				// We connect to and get a message
				String swiftmessage = mqi.receiveMessage(q);
				if (swiftmessage == null || swiftmessage.equals("")) {
					// no message in timeout so loop
					continue;
				}

				// We parse the message
				SWIFTcoder c = new SWIFTcoder(swiftmessage);

				c.summarize();

				// We update the account
				Account a = myBank.getAccountByAccountNumber(c.getDestAccount());
				if (a == null) {
					System.err.println("Failed to find account " + c.getDestAccount() + " in bank " + myBank.getName());
					failC++;
					if (failC > 4) {
						System.err.println("Failed too many times. Quitting");
						stop = true;
					}
					continue;
				}
				a.addMoney(c.getAmmount());
			} catch (Exception e) {
				failC++;
				if (failC > 4) {
					System.err.println("Failed too many times. Quitting");
					stop = true;
				}
				e.printStackTrace();
				continue;
			}
		}
		active = false;
		System.out.println("Receiving thread for bank " + myBank.getSWIFTName() + " now stopped.");
	}

	@Override
	public boolean isActive() {
		return active;
	}
}

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

/**
 * This class handles simulating money being sent from a bank to another bank.
 */
public class MoneySender implements MoneyHandlers {

	private static final int RATE_MIN = 2; // seconds
	private static final int RATE_MAX = 7; // seconds

	private MQI mqi;
	private Bank myBank;
	private ArrayList<Bank> otherBanks;
	private int sessionCode;

	private boolean stop = false;
	private boolean active = false;

	/**
	 * Creates a MoneySender supplying a MQI object to use for communicating to MQ
	 * and the Bank object that it is for.
	 * 
	 * @param mqi The object to use for communicating with IBM MQ.
	 * @param me  The bank this MoneySender will be operating on.
	 * @throws Exception
	 */
	public MoneySender(MQI mqi, Bank me) throws Exception {
		if (mqi == null || me == null) {
			throw new Exception("Invalid parms. mqi or me null");
		}
		Random random = new Random();
		this.mqi = mqi;
		this.myBank = me;
		otherBanks = new ArrayList<Bank>();
		sessionCode = random.nextInt(10000);
	}

	/**
	 * Adds another bank that the MoneySender can send money to.
	 * 
	 * @param b The other bank.
	 * @throws Exception
	 */
	public void addOtherBank(Bank b) throws Exception {
		if (b == null) {
			throw new Exception("Invalid parms. b null");
		}
		otherBanks.add(b);
	}

	@Override
	public void signalStop() {
		stop = true;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	/**
	 * The main loop for this logic, will be ran in a thread.
	 * This loop will run until it is signalled to stop or encounters too many
	 * errors.
	 * 
	 * In a loop it will:
	 * 1. Select a random account from its bank to send money.
	 * 2. Deduct a random amount of money from the acount to send. If the account
	 * has no money it will return to step 1.
	 * 3. Select a random bank to send money to.
	 * 4. Select a random account to recieve the money.
	 * 5. Generate a SWIFTcoder object with the appropriate details.
	 * 6. Send the SWIFT MT103 message generated from the SWIFTcoder object to the
	 * queue for the bank selected in step 3.
	 */
	@Override
	public void run() {
		if (otherBanks.size() == 0) {
			System.err.println("Cannot start as other banks 0.");
			return;
		}
		Random random = new Random();
		active = true;
		String sendBank = myBank.getSWIFTName();
		String sendBranch = myBank.getBranchCode();
		int failC = 0;
		int seq = 0;
		Currency currency = myBank.getDefaultCur();

		System.out.println("Sending thread for bank " + sendBank + " now active.");
		while (stop == false) {
			String sendAccount;
			String sendName;
			String destBank;
			String destAccount;
			String destName;
			String destBranch;
			int ammount;
			String queue;

			// What lucky person will be the sender
			Account sender = myBank.getRandomAccount();
			ammount = sender.subRandomMoney();
			if (ammount == 0) {
				// Can't send a zero amount. Person is poor.
				try {
					int sleepT = random.nextInt(RATE_MAX - RATE_MIN);
					sleepT += RATE_MIN;
					Thread.sleep(sleepT * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			sendAccount = sender.getNumber();
			sendName = sender.getName();

			// Find a random bank to send to, fill in the fields
			Bank receiveB = otherBanks.get(random.nextInt(otherBanks.size()));
			destBank = receiveB.getSWIFTName();
			destBranch = receiveB.getBranchCode();
			queue = receiveB.getqName();
			Account recA = receiveB.getRandomAccount();
			destAccount = recA.getNumber();
			destName = recA.getName();

			// now send the message!
			try {
				SWIFTcoder coder = new SWIFTcoder(sendBank, sendAccount, sendName, destBank, destAccount, destName,
						ammount, currency, sendBranch, destBranch, sessionCode, seq);
				mqi.sendMessage(queue, coder.getMessage());

			} catch (Exception e) {
				System.err.println("Failed to send money from " + sendBank + " to Queue " + queue);
				failC++;
				if (failC > 4) {
					System.err.println("Failed too many times. Quitting");
					stop = true;
				}
			}
			seq++;

			// Sleep before another!
			try {
				int sleepT = random.nextInt(RATE_MAX - RATE_MIN);
				sleepT += RATE_MIN;
				Thread.sleep(sleepT * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		active = false;
		System.out.println("Sending thread for bank " + sendBank + " now stopped.");
	}
}

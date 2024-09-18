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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Assisted by WCA@IBM
// Latest GenAI contribution: ibm/granite-8b-code-instruct
/**
 * This class represents an account with a name, number, and balance. It
 * provides methods to get the name, number, and balance, as well as to add and
 * subtract money from the account. The class also includes a method to print
 * out the account information.
 */
public class Account {

	// As multiple threads could be trying to update the balance we use a lock to
	// make sure we get no issues.
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private String name;
	private String number;
	private int balance;

	// Assisted by WCA@IBM
	// Latest GenAI contribution: ibm/granite-8b-code-instruct
	/**
	 * Creates an account with the given name, given account number and a fixed
	 * balance of 1000.
	 * 
	 * @param name   The name of the account holder.
	 * @param number The account number.
	 */
	public Account(String name, String number) {
		this.name = name;
		this.number = number;
		this.balance = 1000;
	}

	public Account(String name, String number, int startingBalance) {
		this.name = name;
		this.number = number;
		this.balance = startingBalance;
	}

	/**
	 * Returns the name of the account holder under lock.
	 * 
	 * @return The name of the account holder.
	 */
	public String getName() {
		String myname;
		lock.readLock().lock();
		myname = name;
		lock.readLock().unlock();
		return myname;
	}

	/**
	 * Returns the account number under lock.
	 * 
	 * @return The account number.
	 */
	public String getNumber() {
		String mynum;
		lock.readLock().lock();
		mynum = number;
		lock.readLock().unlock();
		return mynum;
	}

	/**
	 * Returns the current balance under lock.
	 * 
	 * @return current balance value
	 */
	public int getBalance() {
		int mybal;
		lock.readLock().lock();
		mybal = balance;
		lock.readLock().unlock();
		return mybal;
	}

	/**
	 * Deposits the given amount into the account.
	 * 
	 * @param amount The amount to deposit.
	 */
	public void addMoney(int add) {
		lock.writeLock().lock();
		if (add < 0) {
			System.out.println("addMoney - unable as add negative");
			lock.writeLock().unlock();
			return;
		}
		balance += add;
		lock.writeLock().unlock();
	}

	/**
	 * Subtracts the given amount from the account.
	 * 
	 * @param amount The amount to deposit.
	 * @return True if it was succesfull.
	 */
	public boolean subMoney(int add) {
		lock.writeLock().lock();
		if (add < 0) {
			System.out.println("subMoney - unable as add negative");
			lock.writeLock().unlock();
			return false;
		}
		if (balance - add < 0) {
			lock.writeLock().unlock();
			return false;
		}
		balance -= add;
		lock.writeLock().unlock();
		return true;
	}

	/**
	 * Subtracts a random amount of money from the account.
	 * The random value can be between 1 - current balance.
	 * 
	 * @return The amount deducted.
	 */
	public int subRandomMoney() {
		Random random = new Random();
		int lost = 0;
		lock.writeLock().lock();
		if (balance != 0) {
			// Only subtract if balance is bigger than 0
			lost = random.nextInt(balance + 1);

			if (balance - lost < 0) {
				lost = balance;
			}
			balance -= lost;
		}
		lock.writeLock().unlock();
		return lost;
	}

	/**
	 * Simple debug function that prints out the details.
	 */
	public void printMe() {
		System.out.println("name[" + name + "] number[" + number + "] balance[" + balance + ",00]");
	}
}

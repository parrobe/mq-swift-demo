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
 * Currency Enum for support currencys and their short strings.
 */
public enum Currency {
	EUR("EUR"), GBP("GBP"), USD("USD");

	private String swiftCode;

	private Currency(String swiftCode) {
		this.swiftCode = swiftCode;
	}

	/**
	 * Returns the string version of the currency to put into the SWIFT message
	 * 
	 * @param swiftCode
	 */
	public String getSwiftCode() {
		return swiftCode;
	}

	/**
	 * Converts a given String into the currency enum.
	 * 
	 * @param string The currency string to convert
	 * @return A currency enum.
	 * @throws Exception
	 */
	public static Currency parse(String string) throws Exception {
		if (string.equalsIgnoreCase("EUR")) {
			return Currency.EUR;
		} else if (string.equalsIgnoreCase("GBP")) {
			return Currency.GBP;
		} else if (string.equalsIgnoreCase("USD")) {
			return Currency.USD;
		} else {
			throw new Exception("Unknown currency " + string);
		}
	}

}

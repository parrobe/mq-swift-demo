# mq-swift-demo
This repository contains a small java demo program that connects to a IBM MQ queue manager and sends SWIFT MT103 messages. It was used to create a semi-realistic messaging demo that was used when demonstrating connecting IBM MQ with Watsonx to process message data.

## Building the demo program
To build the java program you must have access to the IBM MQ java libraries. It is then built using:
```
$ cd src\swiftdemoapp
$ javac *.java
```

## IBM MQ configuration
The demo program makes an assumption about the IBM MQ queue manager configuration. It will connect to a queue manager running on `localhost:1414` using channel `IN`. It will not provide a TLS connection or user credentials. Once connected it will access the following 3 queues. `BANKROB.Q`, `BANKGRA.Q` and `BANKNICK.Q`.

## Demo program usage
The demo program is ran by the following command, you must have access to the IBM MQ java libraries:
```
$ cd src 
$ java -cp ".;<path to IBM MQ install>\java\lib\com.ibm.mq.allclient.jar" swiftdemoapp.Main
```

### Example SWIFT MT103 message
Below is an example of the MT103 messages that are sent and received by the demo program.
```
{1:F01BANKBEBBAXXX1234567890}{2:O1031130050901BANKBEBBAXXX12345678900509011311N}{3:{108:MT103}}{4:
:20:REFERENCE12345
:23B:CRED
:32A:230501EUR123456,78
:50A:/12345678901234567890MR. JOHN DOE
:59:/23456789012345678901MS. JANE SMITH
:70:INVOICE 987654
:71A:SHA
-}
```

### Example output
Below is an example output from the program. 

```
-- Start Bank --
BANK: BankOfRob
SWIFT: BANKROBE
QNAME: BANKROB.Q
CURRENCY: GBP
Accounts: 4
  Account 0: name[Rob Parker] number[82637563171788260071] balance[1000,00]
  Account 1: name[Jimbo Blooms] number[77362153045865116651] balance[1000,00]
  Account 2: name[Dwayne Johnson] number[82453770211081830023] balance[1000,00]
  Account 3: name[Richard Liesen] number[01450838482427365462] balance[1000,00]
-- End Bank --
-- Start Bank --
BANK: BankOfGraham
SWIFT: BANKGRAH
QNAME: BANKGRA.Q
CURRENCY: GBP
Accounts: 4
  Account 0: name[Harry Houdini] number[53663133442278307675] balance[1000,00]
  Account 1: name[Margret Allens] number[45767717258301345051] balance[1000,00]
  Account 2: name[Alice Baker] number[48153635350853384157] balance[1000,00]
  Account 3: name[Sherlock Holmes] number[84884654008541070671] balance[1000,00]
-- End Bank --
-- Start Bank --
BANK: BankOfNick
SWIFT: BANKNICK
QNAME: BANKNICK.Q
CURRENCY: GBP
Accounts: 4
  Account 0: name[David Ware] number[05636756152461537362] balance[1000,00]
  Account 1: name[Amanda Maidstone] number[60018640070064727506] balance[1000,00]
  Account 2: name[Paul Norfolk] number[20585011677507262664] balance[1000,00]
  Account 3: name[Charlie Chesire] number[00471404561666735085] balance[1000,00]
-- End Bank --
Starting all threads
Receiving thread for bank BANKROBE now active.
Receiving thread for bank BANKGRAH now active.
Receiving thread for bank BANKNICK now active.
Sending thread for bank BANKROBE now active.
Sending thread for bank BANKGRAH now active.
Sending thread for bank BANKNICK now active.
Press ENTER to continue...
BANKGRAH/Sherlock/52,00GBP->BANKNICK/Charlie
BANKNICK/Paul/523,00GBP->BANKROBE/Jimbo
BANKROBE/Dwayne/881,00GBP->BANKNICK/Amanda
< ...SNIP... >
BANKGRAH/Margret/667,00GBP->BANKNICK/Charlie

Ending all threads
Receiving thread for bank BANKGRAH now stopped.
Receiving thread for bank BANKROBE now stopped.
Receiving thread for bank BANKNICK now stopped.
Sending thread for bank BANKNICK now stopped.
Sending thread for bank BANKROBE now stopped.
Sending thread for bank BANKGRAH now stopped.
All threads closed. Stopping.
Final stats
-- Start Bank --
BANK: BankOfRob
SWIFT: BANKROBE
QNAME: BANKROB.Q
CURRENCY: GBP
Accounts: 4
  Account 0: name[Rob Parker] number[82637563171788260071] balance[44,00]
  Account 1: name[Jimbo Blooms] number[77362153045865116651] balance[0,00]
  Account 2: name[Dwayne Johnson] number[82453770211081830023] balance[5926,00]
  Account 3: name[Richard Liesen] number[01450838482427365462] balance[1,00]
-- End Bank --
-- Start Bank --
BANK: BankOfGraham
SWIFT: BANKGRAH
QNAME: BANKGRA.Q
CURRENCY: GBP
Accounts: 4
  Account 0: name[Harry Houdini] number[53663133442278307675] balance[237,00]
  Account 1: name[Margret Allens] number[45767717258301345051] balance[548,00]
  Account 2: name[Alice Baker] number[48153635350853384157] balance[348,00]
  Account 3: name[Sherlock Holmes] number[84884654008541070671] balance[1458,00]
-- End Bank --
-- Start Bank --
BANK: BankOfNick
SWIFT: BANKNICK
QNAME: BANKNICK.Q
CURRENCY: GBP
Accounts: 4
  Account 0: name[David Ware] number[05636756152461537362] balance[217,00]
  Account 1: name[Amanda Maidstone] number[60018640070064727506] balance[253,00]
  Account 2: name[Paul Norfolk] number[20585011677507262664] balance[2294,00]
  Account 3: name[Charlie Chesire] number[00471404561666735085] balance[674,00]
-- End Bank --
```

## Health Warning
These programs are provided as-is with no guarantees of support or updates. There are
also no guarantees of compatibility with any future versions of IBM MQ .

## Issues
For feedback and issues relating specifically to this package, please use the [GitHub issue tracker](https://github.com/parrobe/mq-swift-demo/issues).

## Copyright

Copyright Rob Parker 2024

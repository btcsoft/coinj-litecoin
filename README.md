### Welcome to coinj-litecoin

This is implementation of Coinj project inner API [coinj](https://github.com/btcsoft/coinj). 

It may be used as a standalone library to access Litecoin network and managing your own wallet. Coinj is a fork of Bitcoinj project therefore this library offers exactly the same features.

Or it may be used in conjunction with another implementation of Coinj inner API (e.g. [coinj-bitcoin](https://github.com/btcsoft/coinj-bitcoin)) to implement cross-network applications.  

Either way defining features of a whole Coinj stack are easy, clean and standardized alt-coin Java libraries development and crypto-coin cross-network development.

### Technologies

* Java 6 for the core modules, Java 7 for everything else
* [Maven 3+](http://maven.apache.org) - for building the project
* [Orchid](https://github.com/subgraph/Orchid) - for secure communications over [TOR](https://www.torproject.org)
* [Google Protocol Buffers](https://code.google.com/p/protobuf/) - for use with serialization and hardware communications
* [BitcoinJ](https://github.com/bitcoinj/bitcoinj) - upstream library, inner API of which was made alt-coins friendly and less static constants oriented

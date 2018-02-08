# CSV Crypt

[![Build Status](https://travis-ci.org/life-research/csv-crypt.svg?branch=master)](https://travis-ci.org/life-research/csv-crypt)
[![Dependencies Status](https://versions.deps.co/life-research/csv-crypt/status.svg)](https://versions.deps.co/life-research/csv-crypt)

A command line tool which encrypts a CSV file line by line so that it's possible to exchange identifiers without being able to inspect the rest of the data.

## Install

Download an archive for your OS:

* Windows
* Linux

Unpack the archive. It will create a directory called `csv-crypt-0.1`. Open console in this directory and run `csv-crypt`.

## Usage

### Generate Encryption Key

Generates a random key with 32 bytes of entropy using a cryptographically secure random number generator.

```sh
csv-crypt --gen-key
```

### Encrypt a CSV File

```sh
./csv-crypt --encrypt --key <key> <in-file> <out-file> 
```

### Decrypt a CSV File

```sh
./csv-crypt --decrypt --key <key> <in-file> <out-file> 
```

## Build

To create a ZIP for Windows and a tar.gz for Linux run:

```sh
make all
```

The files will be in `target/win` and `target/linux`.

## License

Copyright © 2017 Leipzig Research Centre for Civilization Diseases (LIFE)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

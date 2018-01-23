# CSV Crypt

[![Dependencies Status](https://versions.deps.co/life-research/csv-crypt/status.svg)](https://versions.deps.co/life-research/csv-crypt)

A command line tool which encrypts a CSV file line by line so that it's possible to exchange identifiers without being able to inspect the rest of the data.

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

```sh
make csv-crypt
```

## License

Copyright © 2017 Leipzig Research Centre for Civilization Diseases (LIFE)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

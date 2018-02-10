# CSV Crypt

[![Build Status](https://travis-ci.org/life-research/csv-crypt.svg?branch=master)](https://travis-ci.org/life-research/csv-crypt)
[![Dependencies Status](https://versions.deps.co/life-research/csv-crypt/status.svg)](https://versions.deps.co/life-research/csv-crypt)

A command line tool which encrypts a CSV file line by line so that it's possible to exchange identifiers without being able to inspect the rest of the data.

## Install

Download an archive for your OS:

* [Windows](https://github.com/life-research/csv-crypt/releases/download/v0.2/csv-crypt-0.2.zip)
* [Linux](https://github.com/life-research/csv-crypt/releases/download/v0.2/csv-crypt-0.2.tar.gz)

Unpack the archive. It will create a directory called `csv-crypt-0.2`. Open console in this directory and run `csv-crypt`.

## Usage

```
Usage: csv-crypt [-g] [-e -k key in-file out-file] [-d -k key in-file out-file]
  -k, --key KEY                                32-byte hex encoded key
  -e, --encrypt
  -d, --decrypt
  -g, --gen-key
      --in-encoding ENCODING     UTF-8
      --in-separator SEPARATOR   (default \,)
      --in-tab-separated                       Input file is tab separated
      --out-separator SEPARATOR  (default \,)
      --out-tab-separated                      Output file should be tab separated
      --out-optimize-win                       Optimize the output for Office 2010+
  -h, --help
```

### Generate Encryption Key

Generates a random key with 32 bytes of entropy using a cryptographically secure random number generator.

```sh
csv-crypt --gen-key
```

### Encrypt a CSV File

The default encoding `UFT-8` and the default separator `,` is used.

```sh
csv-crypt --encrypt --key <key> <in-file> <out-file> 
```

### Decrypt a CSV File

The default encoding `UFT-8` and the default separator `,` is used.

```sh
csv-crypt --decrypt --key <key> <in-file> <out-file> 
```

### Encrypt an Excel Unicode Text File

Excel can save spreadsheets as an Unicode Text File. This is the safest way to feed a spreadsheet into `csv-crypt`. An example of such a file can be found in the repository as `examples/excel-2013-unicode-text.txt`. Because the file is tab-separated, you have to use the command line switch `--in-tab-separated`:

```sh
csv-crypt --encrypt --key <key> --in-tab-separated <in-file> <out-file> 
```

The output file will be still comma-separated.

### Optimize the Output File for Office 2010+

In order to produce a output file which can be opened in Excel 2010+, you can use the command line switch `--out-optimize-win`. Please be sure to also use the `csv` file extension. The resulting file opens correctly in Excel.

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

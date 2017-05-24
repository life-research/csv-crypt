version = 0.1-SNAPSHOT

target/csv-crypt-$(version)-standalone.jar:
	lein uberjar

csv-crypt-$(version)-standalone.jar: target/csv-crypt-$(version)-standalone.jar
	cp $< $@

csv-crypt-$(version).zip: csv-crypt-$(version)-standalone.jar csv-crypt.bat
	zip $@ $< csv-crypt.bat

csv-crypt: csv-crypt.sh
	cp $< $@
	chmod +x $@

csv-crypt-$(version).tar.gz: csv-crypt-$(version)-standalone.jar csv-crypt
	tar czf $@ $< csv-crypt

clean:
	rm -f csv-crypt-$(version).zip csv-crypt-$(version).tar.gz csv-crypt-$(version)-standalone.jar csv-crypt
	lein clean

.PHONY: clean

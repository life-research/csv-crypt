version = 0.6-SNAPSHOT

target/csv-crypt-$(version)-standalone.jar:
	lein clean
	lein test
	lein clean
	lein uberjar

target/win/csv-crypt-$(version)/csv-crypt.bat: script/csv-crypt.tpl.bat
	dirname $@ | xargs mkdir -p
	sed -e 's/<VERSION>/$(version)/' $< > $@

target/win/csv-crypt-$(version).zip: target/csv-crypt-$(version)-standalone.jar target/win/csv-crypt-$(version)/csv-crypt.bat
	cd target; cp csv-crypt-$(version)-standalone.jar win/csv-crypt-$(version)
	cd target/win; zip csv-crypt-$(version).zip csv-crypt-$(version)/*.jar
	cd target/win; zip --to-crlf csv-crypt-$(version).zip csv-crypt-$(version)/*.bat

target/linux/csv-crypt-$(version)/csv-crypt: script/csv-crypt.tpl.sh
	dirname $@ | xargs mkdir -p
	sed -e 's/<VERSION>/$(version)/' $< > $@
	chmod +x $@

target/linux/csv-crypt-$(version).tar.gz: target/csv-crypt-$(version)-standalone.jar target/linux/csv-crypt-$(version)/csv-crypt
	cd target; cp csv-crypt-$(version)-standalone.jar linux/csv-crypt-$(version)
	cd target/linux; tar czf csv-crypt-$(version).tar.gz csv-crypt-$(version)

all: target/win/csv-crypt-$(version).zip target/linux/csv-crypt-$(version).tar.gz

clean:
	lein clean

.PHONY: clean all

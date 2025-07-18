#Uyarı
// Kod SQL veritabanına bağlı bir java kodu olduğu için sql.connector.jar bulundurmaktadır. Kodu çalıştırmak için sources.txt'yi kendi dosya yapınıza göre düzenleyiniz.
// Düzenleme için dosyaların bulunduğu konumda (windows için) ''dir /s /b src\*.java > sources.txt'' komutunu kullanabilirsiniz.
// Kodu çalıştırmak için jar dosyası ile birlikte derleyin.(Aşağıdaki komutları kullanabilirsiniz).

javac -cp ".:lib/mysql-connector-j-9.3.0.jar" -d out @sources.txt 
java -cp ".;out;lib/mysql-connector-j-9.3.0.jar" server.ServerApp



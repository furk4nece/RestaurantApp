#Uyarı
//Kod SQL veritabanına bağlı bir java kodu olduğu için jar dosyası bulundurmaktadır kodu çalıştırmak için sources.txt'yi kendi dosya yapınıza göre düzenleyiniz.
javac -cp ".:lib/mysql-connector-j-9.3.0.jar" -d out @sources.txt 
java -cp ".;out;lib/mysql-connector-j-9.3.0.jar" server.ServerApp



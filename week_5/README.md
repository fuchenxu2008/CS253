# How to run

1. Open the shell/terminal and sit at root directory.
2. For 17, Execute `cd week_5 && javac Seventeen.java && java Seventeen ../pride-and-prejudice.txt`
3. For 20, with app1, Execute
  - `cd week_5/Twenty/`
  - `javac *.java`
  - `jar cf app1.jar words1.class frequencies1.class`
  - `jar cf app2.jar words2.class frequencies2.class`
  - `jar cfm framework.jar manifest.mf Twenty.class config.properties`
  - `java -cp framework.jar Twenty ../../pride-and-prejudice.txt`
4. For changing to app2, open `config.properties` file, and comment the first section and uncomment the second section
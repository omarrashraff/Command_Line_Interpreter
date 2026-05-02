# Java Command Line Interpreter 🖥️

Developed in collaboration with @minshawi0 as part of the Cryptography course at Cairo University, Faculty of Engineering.

A Java-based command-line interpreter that simulates core Linux/Unix shell commands.  
Built from scratch with no external libraries — pure Java file I/O, parsing, and system operations.

---

## 📌 Features

- Custom input parser (no `String.split()` dependency)
- Navigate and manage the file system via terminal commands
- Redirect output to files using `>` and `>>`
- Compress and extract files using `zip` / `unzip`
- Alphabetically sorted directory listings

---

## 🚀 Supported Commands

| Command | Description | Example |
|--------|-------------|---------|
| `pwd` | Print current working directory | `pwd` |
| `cd` | Change directory | `cd foldername` / `cd ..` / `cd` |
| `ls` | List files and folders (alphabetically) | `ls` |
| `mkdir` | Create one or more directories | `mkdir dir1 dir2` |
| `rmdir` | Remove empty directory / all empty dirs | `rmdir dirname` / `rmdir *` |
| `touch` | Create a new empty file | `touch file.txt` |
| `cp` | Copy a file | `cp source.txt dest.txt` |
| `cp -r` | Copy a directory recursively | `cp -r srcDir destDir` |
| `rm` | Delete a file | `rm file.txt` |
| `cat` | Display file contents | `cat file.txt` |
| `wc` | Count lines, words, characters in a file | `wc file.txt` |
| `echo` | Print text to console or file | `echo Hello World` |
| `zip` | Compress files into a `.zip` archive | `zip archive.zip file.txt` |
| `zip -r` | Compress a directory recursively | `zip -r archive.zip folder` |
| `unzip` | Extract a `.zip` archive | `unzip archive.zip` |
| `>` | Redirect output to a file (overwrite) | `ls > output.txt` |
| `>>` | Redirect output to a file (append) | `echo hi >> log.txt` |
| `exit` | Exit the interpreter | `exit` |

---

## 🛠️ How to Run

### Requirements
- Java JDK 8 or higher

### Compile
```bash
javac Terminal.java
```

### Run
```bash
java Terminal
```

You will see the prompt:
```
>>>
```

Start typing commands just like a real terminal.

---

## 📁 Project Structure

```
Java-Command-Line-Interpreter/
│
├── Terminal.java       # Main source file (Terminal + Parser classes)
└── README.md           # Project documentation
```

---

## 💡 Example Session

```
>>> pwd
/home/user/projects

>>> mkdir testFolder
Directory created: /home/user/projects/testFolder

>>> cd testFolder
>>> touch hello.txt
File created: /home/user/projects/testFolder/hello.txt

>>> echo Hello World > hello.txt
Output redirected to: hello.txt

>>> cat hello.txt
Hello World

>>> wc hello.txt
1 2 12 hello.txt

>>> cd ..
>>> rmdir testFolder
Error: Directory is not empty

>>> rm testFolder/hello.txt
File deleted successfully: hello.txt

>>> rmdir testFolder
Directory removed: /home/user/projects/testFolder

>>> exit
Exiting terminal...
```

---

## 🧠 How It Works

### Parser Class
Reads raw user input and splits it into a **command name** and **arguments** using a custom character-by-character parser — no built-in `split()` shortcuts.

### Terminal Class
Receives parsed commands and maps them to Java methods that interact with the real file system using `java.io.File`, `FileInputStream`, `FileOutputStream`, and `java.util.zip`.

### I/O Redirection
Output is captured using `ByteArrayOutputStream` before being written to a file with `FileWriter` in overwrite (`>`) or append (`>>`) mode.

---

## 👨‍💻 Author

Made with ❤️ in Java.  
Feel free to fork, star ⭐, and contribute!

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

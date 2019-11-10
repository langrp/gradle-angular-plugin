# Sample Angular Application

The example project shows initialization of angular project from only gradle installation (create wrapper with
`gradle wrapper`). Angular CLI will raise an error on existed README.md, therefore remove the file first.
```shell script
./gradlew angularInit --style=scss --routing --skipGit
```

Start angular demo application with:
```shell script
./ng serve
```

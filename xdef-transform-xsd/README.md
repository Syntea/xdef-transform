# X-definition <-> XML schema transformation

Transformace mezi formáty X-definice a XML schéma.
* Převod z X-definice do XML schéma může být ztrátový
    * Uživatel je o ztrátové transformaci informován v rámci výstupu algoritmu
* Převod z XML schéma do X-definice by neměl být ztrátový

## Usage

### Spuštení v konzoli
* Je potřeba mít zadefinovanou Javu v cestě systémových proměnných.
* Ukázka spuštění:

```console
java -jar .\xdef-transform-xsd.jar
```

* Do konzole by se mělo vypsat následující:

```console
Missing required options: i, o
usage: X-definition to XSD converter
…
```

Dále pokračují popisy přepínačů...

* **Povinné přepínače jsou:**

    * `i` – adresář obsahující vstupní soubory X-definic
    * `o` – výstupní složka pro XSD

**Príklad kompletního volání**
```console
java -jar .\xdef-transform-xsd.jar -i "C:\syntea\projects\xdefinition\input" -o "C:\syntea\projects\xdefinition\output"
```

* Pokud chcete do výstupního schéma přidat uzly <xs:annotation>, které obsahují základní informace o ztrátových transformacích, tak potom stačí přidat přepínač `-f` s hodnotou `a`. Příklad:
```console
java -jar .\xdef-transform-xsd.jar -i "C:\syntea\projects\xdefinition\input" -o "C:\syntea\projects\xdefinition\output" -f a
```

* Volání je možné rozšířit o validaci XML dat skrze interní nástroj (rozumějme knihovnu) Javy. Pokud chcete zvalidovat nějaký validní (myšleno vůči vstupní X-definici) XML soubor proti výstupu algoritmu (XML schema), potom stačí použít přepínač `-tp`. Je možné najednou testovat i více testovacích datových souboru. Zároveň je nutné pomocí přepínače `-r` určit i název kořenovou X-definice, resp. název XML schema souboru, vůči kterému budou data validována. Příklad volání s jedním datovým souborem:
```console
java -jar .\xdef-transform-xsd.jar -i "C:\syntea\projects\xdefinition\input" -o "C:\syntea\projects\xdefinition\output" -r "sisma" -tp "C:\syntea\projects\xdefinition\data\sisma.xml"
```

## Distribution

Maven command: creating a distribution archive containing the application
```
mvn clean package -Pdist
```
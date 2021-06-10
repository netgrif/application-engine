# Pull request guidelines

Nasledujúce kroky popisujú proces prípravy samotného Pull requestu(PR) a ďalšie nadväzujúce úlohy.
Ihneď po vytvorení PR klikni na "..." (nachádza sa medzi tlačidlami Approve a Settings). Vyber možnosť Pull request guidelines. Po otvorení okna s guidelines, vytvor jednotlivé tasky kliknutím na znak "+". 
Po vytvorení taskov vykonaj postupne jednotlivé úlohy. Každú splnenú úlohu označ zaškrtnutím príslušného tasku. PR je možné schváliť len po zaškrtnutí všetkých taskov.

Viac info nájdeš v [Pull Request Guidelines](https://netgrif.atlassian.net/wiki/spaces/NAE/pages/1528758295/Pull+Request+Guidelines).

## Vyriešenie konfliktov v PR
## Merge cieľovej branche
## Kontrola výsledkov Lint testov
Výsledok je možné vidieť v Jenkinse v detaile buildu, nájdené porušenia pravidiel je potrebné odstrániť.
## Kontrola výsledkov SonarQube analýzy
Výsledok je možné vidieť priamo v detaile PR, nájdené nedostatky je potrebné odstrániť.
## Unit testy
Výsledok je možné vidieť v Jenkinse v detaile buildu, ak pre implementovanú funkcionalitu neexistujú testy, je nevyhnutné ich vytvoriť. Existujúce unit testy je potrebné aktualizovať.
## Integračné/procesné testy
Vytvor mini proces, ktorý demonštruje implementovanú funkcionalitu. Pri úprave už existujúcej funkcionality, aktualizuj proces. Umiestnenie procesu, uveď ako komentár PR.
## Technická dokumentácia
Vytvor dokumentáciu v kóde - pre jednoduchú orientáciu a prípadne použitie treťou stranou.
## Developerská dokumentácia
Napíš článok v knowledgebase (Confluence) - popis funkcionality, príklady použitia, prerekvizity používania.
## Používateľská dokumentácia
Vytvor príručku pre používanie danej funkcionality (ak bolo ovplyvnené UI/UX), jasný popis, názorné príklady.
## Breaking changes/migrácie
Ak implementácia spôsobuje zmenu nekompatibilnú s predchádzajúcimi verziami, spíš do Migration guide stav a rozsah zmeny, spôsob migrácie pre prípad aktualizácie. Ak je možné zmenu automatizovať, uveď kód. Odkaz na dokumentáciu zmien uveď do komentára PR
## Asistované testovanie
Implementáciu je potrebné prezentovať zadávateľovi úlohy, prípadné námietky sa musia zapracovať. Zadávateľa pridaj ako reviewera otvoreného PR. Zadávateľ je ten, kto vytvoril issue, alebo ten na koho podnet bol implementovaný issue vytvorený.

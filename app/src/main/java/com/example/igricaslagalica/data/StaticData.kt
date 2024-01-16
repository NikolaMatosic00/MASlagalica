package com.example.igricaslagalica.data

import com.example.igricaslagalica.model.Asocijacija
import com.example.igricaslagalica.model.Asocijacije
import com.example.igricaslagalica.model.KoZnaZna
import com.example.igricaslagalica.model.KorakPoKorak

class StaticData {
    companion object {

        private val pitanjaKoZnaZnas = listOf(
            KoZnaZna(
                1,
                "Koja zemlja ima najviše piramida?",
                listOf("Egipat", "Meksiko", "Grčka", "Indija"),
                1
            ),
            KoZnaZna(
                2,
                "Koja je najveća jezera na Balkanu?",
                listOf(
                    "Skadarsko jezero",
                    "Ohridsko jezero",
                    "Prespansko jezero",
                    "Vlasinsko jezero"
                ),
                1
            ),
            KoZnaZna(
                3,
                "Koja je najduža planina na svetu?",
                listOf("Andi", "Himalaji", "Apenini", "Stenovite planine"),
                2
            ),
            KoZnaZna(
                4,
                "Koja je najmanja reka u Evropi?",
                listOf("Crvena reka", "Žuta reka", "Plava reka", "Zelena reka"),
                1
            ),
            KoZnaZna(
                5,
                "Koja je najveća umetna jezera na svetu?",
                listOf("Kariba", "Volga", "Talingska brana", "Dnjepr"),
                3
            ),
            KoZnaZna(
                6,
                "Koja je najstarija civilizacija na svetu?",
                listOf(
                    "Majanska civilizacija",
                    "Sumerska civilizacija",
                    "Inka civilizacija",
                    "Kineska civilizacija"
                ),
                2
            ),
            KoZnaZna(
                7,
                "Koja je najhladnija naseljena tačka na Zemlji?",
                listOf("Ojmjakon", "Antarktika", "Vostok", "Barrow"),
                1
            ),
            KoZnaZna(
                8,
                "Koja je najviša zgrada na svetu?",
                listOf("Burdž Kalifa", "Šangaj toranj", "Abraj Al Bajt", "One World Trade Center"),
                1
            ),
            KoZnaZna(
                9,
                "Koja je najstarija planeta u Sunčevom sistemu?",
                listOf("Zemlja", "Mars", "Venera", "Jupiter"),
                4
            ),
            KoZnaZna(
                10,
                "Koja je najveća pećina na svetu?",
                listOf("Mamutova pećina", "Son Doong", "Puerto Princesa", "Krubera-Voronya"),
                2
            ),
            KoZnaZna(
                11,
                "Koja je najveća država na svetu po površini?",
                listOf("Rusija", "Kanada", "Kina", "Sjedinjene Američke Države"),
                1
            ),
            KoZnaZna(
                12,
                "Koja je najduža reka na svetu?",
                listOf("Nil", "Amazona", "Misisipi", "Jangce"),
                1
            ),
            KoZnaZna(
                13,
                "Koja je najviša planina na svetu?",
                listOf("Mont Everest", "K2", "Kraljica Elizabeta", "Makalu"),
                1
            ),
            KoZnaZna(
                14,
                "Koja je najnaseljenija zemlja na svetu?",
                listOf("Kina", "Indija", "Sjedinjene Američke Države", "Indonezija"),
                1
            ),
            KoZnaZna(
                15,
                "Koja je najveća pustinja na svetu?",
                listOf("Sahara", "Gobi", "Atacama", "Antarktika"),
                1
            ),
            KoZnaZna(
                16,
                "Koja je najmanja država na svetu po površini?",
                listOf("Vatikan", "Monako", "Nauru", "Tuvalu"),
                1
            ),
            KoZnaZna(
                17,
                "Koja je najveća svetska okeanska struja?",
                listOf("Golfska struja", "Kuro-šio", "Bengalska struja", "Kanarska struja"),
                1
            ),
            KoZnaZna(
                18,
                "Koja zemlja se nalazi na jugu Afrike?",
                listOf("Južna Afrika", "Alžir", "Egipat", "Nigerija"),
                1
            ),
            KoZnaZna(
                19,
                "Koji kontinent ima najveći broj zemalja?",
                listOf("Afrika", "Azija", "Evropa", "Južna Amerika"),
                1
            ),
            KoZnaZna(
                20,
                "Koji je najveći otok na svetu?",
                listOf("Grönland", "Nova Gvineja", "Borneo", "Madagaskar"),
                1
            )
        )

        private val pitanjaKorakPoKorakList = listOf(
            KorakPoKorak(
                id = 1,
                odgovor = "Piramida",
                pojmovi = listOf(
                    "Arheološki spomenik",
                    "Egipt",
                    "Giza",
                    "Faraon",
                    "Sfinga",
                    "Arhitektura",
                    "Stepenasta konstrukcija"
                )
            ),

            KorakPoKorak(
                id = 2,
                odgovor = "Ekosistem",
                pojmovi = listOf(
                    "Biodiverzitet",
                    "Trofički nivoi",
                    "Biološka ravnoteža",
                    "Očuvanje",
                    "Flora i fauna",
                    "Otrovi",
                    "Habitat"
                )
            ),

            KorakPoKorak(
                id = 3,
                odgovor = "Rudarstvo",
                pojmovi = listOf(
                    "Eksploatacija",
                    "Minerali",
                    "Rudnik",
                    "Metalurgija",
                    "Kopanje",
                    "Resursi",
                    "Ekonomija"
                )
            )

        )

        val asocijacijeData = Asocijacije(
            asocijacije = listOf(
                Asocijacija(
                    asocijacijaList = listOf(
                        "Koloseum",
                        "Kula Neviđanščina",
                        "Venecija",
                        "Mikelanđelo"
                    ),
                    asocijacijaResenje = "Italijanski spomenici"
                ),
                Asocijacija(
                    asocijacijaList = listOf(
                        "Fudžijama",
                        "Sumo rvanje",
                        "Cvetanje trešanja",
                        "Samuraji"
                    ),
                    asocijacijaResenje = "Japanski simboli"
                ),
                Asocijacija(
                    asocijacijaList = listOf("Sombrero", "Tekila", "Mariachi", "Chichen Itza"),
                    asocijacijaResenje = "Meksički simboli"
                ),
                Asocijacija(
                    asocijacijaList = listOf("Tadž Mahal", "Sari", "Boliivud", "Ganeša"),
                    asocijacijaResenje = "Indijski simboli"
                )
            ),
            asocijacijaKonacnoResenje = "Simboli"
        )

        fun dajAsocijaciju(): Asocijacije {
            return asocijacijeData
        }

        fun dajPitanjeKorakPoKorak(): List<KorakPoKorak> {
            return pitanjaKorakPoKorakList.shuffled()
        }

        fun dajPitanjaKoZnaZna(): List<KoZnaZna> {
            return pitanjaKoZnaZnas
        }
    }
}


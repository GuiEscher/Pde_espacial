package br.ufscar.pde;

import java.io.FileReader;
import java.io.IOException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.ParseException;

public class Jts {

    public Jts() {
        // System.out.println("Objt criado");
    }

    public static void main(String[] args) {
        System.out.println("Objt criado");

        WKTReader reader = new WKTReader(new GeometryFactory());

        String csvFile = "I:\\94b_buildings (2).csv";

        try (CSVReader csvReader = new CSVReader(new FileReader(csvFile))) {
            String[] nextLine;
            int lineCount = 0;
            while ((nextLine = csvReader.readNext()) != null) {
                lineCount++;
                if (lineCount >= 2) {
                    // latitude,longitude,area_in_meters,confidence,geometry,full_plus_code
                    if (nextLine.length < 6) {
                        System.err.println("Número insuficiente de colunas na linha " + lineCount);
                        continue;
                    }

                    String latitude = nextLine[0];
                    String longitude = nextLine[1];
                    String areaInMeters = nextLine[2];
                    String confidence = nextLine[3];
                    String wkt = nextLine[4];
                    String fullPlusCode = nextLine[5];

                    if (wkt == null || wkt.trim().isEmpty()) {
                        System.err.println("Campo WKT vazio na linha " + lineCount);
                        continue;
                    }

                    try {
                        Geometry geometry = reader.read(wkt);
                        // Processar a geometria conforme necessário
                        System.out.println("Geometria lida: " + geometry);
                    } catch (ParseException e) {
                        System.err.println("Erro ao ler WKT na linha " + lineCount + ": " + e.getMessage());
                    }
                }
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }
}

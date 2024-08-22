package br.ufscar.pde;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PApplet;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.utils.MapUtils;

public class Jts extends PApplet {

    UnfoldingMap map;
    Map<String, Envelope> citiesBounds = new HashMap<>();
    WKTReader reader;

    public static void main(String[] args) {
        PApplet.main("br.ufscar.pde.Jts");
    }

    public void settings() {
        // Defina um tamanho maior para a janela
        size(1600, 900);
    }

    public void setup() {
    	
        // Inicializa o mapa Unfolding
        map = new UnfoldingMap(this, new Microsoft.AerialProvider());
        MapUtils.createDefaultEventDispatcher(this, map); // Adiciona interação com o mapa
       // this.background(0, 0, 128);
        // Definindo os limites de várias cidades
        citiesBounds.put("São Carlos", new Envelope(new Coordinate(-47.951247, -22.077147), new Coordinate(-47.840868, -21.959933)));
        citiesBounds.put("Ribeirão Preto", new Envelope(new Coordinate(-47.899715, -21.281961), new Coordinate(-47.705195, -21.076607)));
        citiesBounds.put("São José do Rio Preto", new Envelope(new Coordinate(-49.46379, -20.898206), new Coordinate(-49.301444, -20.735176)));
        citiesBounds.put("Uberlândia", new Envelope(new Coordinate(-48.387275, -19.008029), new Coordinate(-48.140105, -18.836115)));
        citiesBounds.put("João Pinheiro", new Envelope(new Coordinate(-46.199548,-17.763 ), new Coordinate(-46.159253,-17.724591)));
        citiesBounds.put("Araguari", new Envelope(new Coordinate( -48.226442,-18.680026), new Coordinate(-48.160004,-18.598937)));
        citiesBounds.put("Catalão", new Envelope(new Coordinate(-47.981729,-18.201498), new Coordinate(-47.904562,-18.12796)));
        citiesBounds.put("Barretos", new Envelope(new Coordinate(-48.613548, -20.605287), new Coordinate(-48.540156, -20.509355)));
        citiesBounds.put("Bauru", new Envelope(new Coordinate(-49.204867 , -22.407726), new Coordinate(-48.97623, -22.167988)));
        citiesBounds.put("Ituiutaba", new Envelope(new Coordinate(-49.502015, -19.012515), new Coordinate(-49.4275, -18.934442)));
        citiesBounds.put("Caldas Novas", new Envelope(new Coordinate(-48.671354, -17.803662), new Coordinate(-48.160004,-17.64988)));
        citiesBounds.put("Paracatu", new Envelope(new Coordinate(-46.906856,-17.245327), new Coordinate(-46.841595,-17.203027)));
        citiesBounds.put("Patrocínio", new Envelope(new Coordinate(-47.023814,-18.96782), new Coordinate(-46.945663,-18.900245)));
        citiesBounds.put("Patos de Minas", new Envelope(new Coordinate(-46.542056,-18.63742), new Coordinate(-46.470516,-18.553992)));
        citiesBounds.put("Uberaba", new Envelope(new Coordinate(-48.030804,-19.815321), new Coordinate(-47.868262,-19.691525)));
        citiesBounds.put("Franca", new Envelope(new Coordinate(-47.548032,-20.772015), new Coordinate(-47.300304, -20.41549)));
        citiesBounds.put("Araxá", new Envelope(new Coordinate(-46.970673,-19.654212), new Coordinate(-46.90554,-19.555112)));

        reader = new WKTReader(new GeometryFactory());

        String csvFile = "I:\\94b_buildings (2).csv";

        // Criando mapas para armazenar contagens e áreas por cidade
        Map<String, Integer> cityBuildingCounts = new HashMap<>();
        Map<String, Double> cityAreaSums = new HashMap<>();

        try (CSVReader csvReader = new CSVReader(new FileReader(csvFile))) {
            String[] nextLine;
            int lineCount = 0;
            while ((nextLine = csvReader.readNext()) != null) {
                lineCount++;
                if (lineCount >= 2) {
                    if (nextLine.length < 6) {
                        System.err.println("Número insuficiente de colunas na linha " + lineCount);
                        continue;
                    }

                    String confidence = nextLine[3];
                    float confidence2 = Float.parseFloat(confidence);

                    String areaInMeters = nextLine[2];
                    String wkt = nextLine[4];

                    if (wkt == null || wkt.trim().isEmpty()) {
                        System.err.println("Campo WKT vazio na linha " + lineCount);
                        continue;
                    }

                    try {
                        Geometry geometry = reader.read(wkt);

                        // Itera sobre cada cidade para verificar em qual ela se encontra
                        for (Map.Entry<String, Envelope> cityEntry : citiesBounds.entrySet()) {
                            String cityName = cityEntry.getKey();
                            Envelope cityBounds = cityEntry.getValue();

                            // Verifica se a geometria está dentro dos limites da cidade
                            if (geometry.getEnvelopeInternal().intersects(cityBounds) && confidence2 > 0.85) {
                                cityBuildingCounts.put(cityName, cityBuildingCounts.getOrDefault(cityName, 0) + 1);
                                cityAreaSums.put(cityName, cityAreaSums.getOrDefault(cityName, 0.0) + Double.parseDouble(areaInMeters));

                                // Obtendo o centro da geometria para plotar no mapa
                                Coordinate coord = geometry.getCentroid().getCoordinate();
                                Location location = new Location(coord.y, coord.x);

                                // Criando e adicionando o marcador ao mapa
                                SimplePointMarker marker = new SimplePointMarker(location);
                                map.addMarker(marker);
                            }
                        }

                    } catch (ParseException e) {
                        System.err.println("Erro ao ler WKT na linha " + lineCount + ": " + e.getMessage());
                    }
                }
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }

        // Exibindo os resultados por cidade
        for (String city : citiesBounds.keySet()) {
            int count = cityBuildingCounts.getOrDefault(city, 0);
            float totalArea = (float) (cityAreaSums.getOrDefault(city, 0.0) / 1_000_000);
            System.out.println("Cidade: " + city + " | Número de prédios: " + count + " | Área total: " + totalArea + " km quadrados");
        }

        // Ajustando o mapa para um nível de zoom que mostre as cidades
        map.zoomAndPanTo(6, new Location(-22.0, -47.0)); // Definindo um ponto central no mapa
    }

    public void draw() {
    	background(240);
        map.draw();
    }
}

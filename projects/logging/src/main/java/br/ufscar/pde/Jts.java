package br.ufscar.pde;

import javax.swing.*;
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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class Jts extends PApplet {

    UnfoldingMap map;
    Map<String, Envelope> citiesBounds = new HashMap<>();
    WKTReader reader;
    Map<String, Integer> cityBuildingCounts = new HashMap<>();
    Map<String, Double> cityAreaSums = new HashMap<>();
    Map<String, Integer> cityPopulations = new HashMap<>();
    Map<String, Double> cityDensity = new HashMap<>();

    public static void main(String[] args) {
        PApplet.main("br.ufscar.pde.Jts");
    }

    public void settings() {
        size(1600, 900, P2D);
        System.out.println("Renderer escolhido: " + this.sketchRenderer());
    }

    public void setup() {
    	
    	// Inicializa o mapa Unfolding
        map = new UnfoldingMap(this, new Microsoft.AerialProvider());
        MapUtils.createDefaultEventDispatcher(this, map);
        
        // Populações das cidades
        cityPopulations.put("São Carlos", 254857);
        cityPopulations.put("Ribeirão Preto", 698642);
        cityPopulations.put("São José do Rio Preto", 480393);
        cityPopulations.put("Uberlândia", 713224);
        cityPopulations.put("João Pinheiro", 46801);
        cityPopulations.put("Araguari", 117808);
        cityPopulations.put("Catalão", 114427);
        cityPopulations.put("Barretos", 122485);
        cityPopulations.put("Bauru", 379146);
        cityPopulations.put("Ituiutaba", 102217);
        cityPopulations.put("Caldas Novas", 98622);
        cityPopulations.put("Paracatu", 94023);
        cityPopulations.put("Patrocínio", 89826);
        cityPopulations.put("Patos de Minas", 159235);
        cityPopulations.put("Uberaba", 337836);
        cityPopulations.put("Franca", 352536);
        cityPopulations.put("Araxá", 111691);

        // Definindo os limites de várias cidades
        citiesBounds.put("São Carlos", new Envelope(new Coordinate(-47.951247, -22.077147), new Coordinate(-47.840868, -21.959933)));
        citiesBounds.put("Ribeirão Preto", new Envelope(new Coordinate(-47.899715, -21.281961), new Coordinate(-47.705195, -21.076607)));
        citiesBounds.put("São José do Rio Preto", new Envelope(new Coordinate(-49.46379, -20.898206), new Coordinate(-49.301444, -20.735176)));
        citiesBounds.put("Uberlândia", new Envelope(new Coordinate(-48.387275, -19.008029), new Coordinate(-48.140105, -18.836115)));
        citiesBounds.put("João Pinheiro", new Envelope(new Coordinate(-46.199548, -17.763), new Coordinate(-46.159253, -17.724591)));
        citiesBounds.put("Araguari", new Envelope(new Coordinate(-48.226442, -18.680026), new Coordinate(-48.160004, -18.598937)));
        citiesBounds.put("Catalão", new Envelope(new Coordinate(-47.981729, -18.201498), new Coordinate(-47.904562, -18.12796)));
        citiesBounds.put("Barretos", new Envelope(new Coordinate(-48.613548, -20.605287), new Coordinate(-48.540156, -20.509355)));
        citiesBounds.put("Bauru", new Envelope(new Coordinate(-49.204867, -22.407726), new Coordinate(-48.97623, -22.167988)));
        citiesBounds.put("Ituiutaba", new Envelope(new Coordinate(-49.502015, -19.012515), new Coordinate(-49.4275, -18.934442)));
        citiesBounds.put("Caldas Novas", new Envelope(new Coordinate(-48.671354, -17.803662), new Coordinate(-48.160004, -17.64988)));
        citiesBounds.put("Paracatu", new Envelope(new Coordinate(-46.906856, -17.245327), new Coordinate(-46.841595, -17.203027)));
        citiesBounds.put("Patrocínio", new Envelope(new Coordinate(-47.023814, -18.96782), new Coordinate(-46.945663, -18.900245)));
        citiesBounds.put("Patos de Minas", new Envelope(new Coordinate(-46.542056, -18.63742), new Coordinate(-46.470516, -18.553992)));
        citiesBounds.put("Uberaba", new Envelope(new Coordinate(-48.030804, -19.815321), new Coordinate(-47.868262, -19.691525)));
        citiesBounds.put("Franca", new Envelope(new Coordinate(-47.548032, -20.772015), new Coordinate(-47.300304, -20.41549)));
        citiesBounds.put("Araxá", new Envelope(new Coordinate(-46.970673, -19.654212), new Coordinate(-46.90554, -19.555112)));

        reader = new WKTReader(new GeometryFactory());

        // Lendo o arquivo Csv
        String csvFile = "I:\\94b_buildings (2).csv";

        try (CSVReader csvReader = new CSVReader(new FileReader(csvFile))) {
            String[] nextLine;
            int lineCount = 0;
            
            // Inicia a leitura do csv, linha por linha
            while ((nextLine = csvReader.readNext()) != null) {
                lineCount++;
                if (lineCount >= 2) {
                    if (nextLine.length < 6) {
                        System.err.println("Número insuficiente de colunas na linha " + lineCount);
                        continue;
                    }

                    // Pega a confidence em string e transforma pra float
                    String confidence = nextLine[3];
                    float confidence2 = Float.parseFloat(confidence);

                    // Faz o mesmo cm a área
                    String areaInMeters = nextLine[2];
                    String wkt = nextLine[4];

                    if (wkt == null || wkt.trim().isEmpty()) {
                        System.err.println("Campo WKT vazio na linha " + lineCount);
                        continue;
                    }
                    
                    
                    // Pega o campo que define o polígono de cada cidade e armazena em uma geometry
                    try {
                        Geometry geometry = reader.read(wkt);
                        
                     // Itera sobre cada cidade para verificar em qual ela se encontra
                        for (Map.Entry<String, Envelope> cityEntry : citiesBounds.entrySet()) {
                            String cityName = cityEntry.getKey();
                            Envelope cityBounds = cityEntry.getValue();

                            // Verifica se a geometria está dentro dos limites da cidade
                            if (geometry.getEnvelopeInternal().intersects(cityBounds) && confidence2 > 0.75) {
                                cityBuildingCounts.put(cityName, cityBuildingCounts.getOrDefault(cityName, 0) + 1);
                                cityAreaSums.put(cityName, cityAreaSums.getOrDefault(cityName, 0.0) + Double.parseDouble(areaInMeters));
                                
                                // Obtendo o centro da geometria para plotar no mapa
                                Coordinate coord = geometry.getCentroid().getCoordinate();
                                Location location = new Location(coord.y, coord.x);

                                // Criando e adicionando o marcador ao mapa
                                SimplePointMarker marker = new SimplePointMarker(location);
                                map.addMarker(marker);
                                marker.setRadius(10);
                                marker.setColor(RGB);
                                map.setInnerScale((float) 200.0);
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
        map.zoomAndPanTo(6, new Location(-22.0, -47.0));

        // Criando gráficos com JFreeChart
        createCityAreaChart(cityAreaSums);
        createCityBuildingCountChart(cityBuildingCounts);
        createPopulationPerBuildingChart(cityPopulations, cityBuildingCounts);
        createCityDensityChart(cityBuildingCounts, cityAreaSums, cityPopulations);
    }

    public void draw() {
        background(240);
        map.draw();
    }
    
    // Função para criar gráfico de habitantes por edifício
    private void createPopulationPerBuildingChart(Map<String, Integer> cityPopulations, Map<String, Integer> cityBuildingCounts) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Calculando o número de habitantes por edificio 
        for (Map.Entry<String, Integer> entry : cityPopulations.entrySet()) {
            String city = entry.getKey();
            int population = entry.getValue();
            int buildingCount = cityBuildingCounts.getOrDefault(city, 0);
            if (buildingCount > 0) {
                double populationPerBuilding = (double) population / buildingCount;
                dataset.addValue(populationPerBuilding, "Habitantes por Edifício", city);
            }
        }

        // Montando o gráfico
        JFreeChart barChart = ChartFactory.createBarChart(
                "Habitantes por Edifício por Cidade",
                "Cidade",
                "Habitantes por Edifício",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(barChart);
        JFrame frame = new JFrame("Population Per Building");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.add(chartPanel);
        frame.setVisible(true);
    }

    private void createCityAreaChart(Map<String, Double> cityAreaSums) {
    	// Montando o dataset com o área total ocupada por prédios na cidade
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> entry : cityAreaSums.entrySet()) {
            dataset.addValue(entry.getValue() / 1_000_000, "Área (km²)", entry.getKey());
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Área ocupada por prédios por cidade",
                "Cidade",
                "Área (km²)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(barChart);
        JFrame frame = new JFrame("City Area Statistics");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.add(chartPanel);
        frame.setVisible(true);
    }

    private void createCityBuildingCountChart(Map<String, Integer> cityBuildingCounts) {
    	// Montando o dataset com os numeros de predios encontrados em cada cidade
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : cityBuildingCounts.entrySet()) {
            dataset.addValue(entry.getValue(), "Número de Prédios", entry.getKey());
        }

        // Montando o gráfico
        JFreeChart barChart = ChartFactory.createBarChart(
                "Contagem de Prédios por Cidade",
                "Cidade",
                "Número de Prédios",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(barChart);
        JFrame frame = new JFrame("City Building Counts");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.add(chartPanel);
        frame.setVisible(true);
    }
    
    private void createCityDensityChart(Map<String, Integer> cityBuildingCounts, Map<String, Double> cityAreaSums, Map<String, Integer> cityPopulations) {
        // Densidades demográficas fornecidas pelo IBGE
        Map<String, Double> providedDensities = new HashMap<>();
        providedDensities.put("São Carlos", 224.17);
        providedDensities.put("Ribeirão Preto", 1073.32);
        providedDensities.put("São José do Rio Preto", 1112.17);
        providedDensities.put("Uberlândia", 173.31);
        providedDensities.put("João Pinheiro", 4.36);
        providedDensities.put("Araguari", 43.16);
        providedDensities.put("Catalão", 29.90);
        providedDensities.put("Barretos", 78.21);
        providedDensities.put("Bauru", 567.85);
        providedDensities.put("Ituiutaba", 39.34);
        providedDensities.put("Caldas Novas", 61.92);
        providedDensities.put("Paracatu", 15.11);
        providedDensities.put("Patrocínio", 60.97);
        providedDensities.put("Patos de Minas", 105.33);
        providedDensities.put("Uberaba", 92.12);
        providedDensities.put("Franca", 213.31);
        providedDensities.put("Araxá", 27.83);

        // Cálculo das densidades com base nos habitantes e área ocupada por prédios
        Map<String, Double> calculatedDensities = new HashMap<>();
        for (Map.Entry<String, Integer> entry : cityPopulations.entrySet()) {
            String city = entry.getKey();
            int population = entry.getValue();
            double totalBuildingArea = cityAreaSums.getOrDefault(city, 0.0) / 1_000_000; // Área ocupada por prédios em km²
            if (totalBuildingArea > 0) {
                double density = population / totalBuildingArea; // Habitantes por km² ocupada por prédios
                calculatedDensities.put(city, density);
            }
        }

        // Criação do dataset para preencher o banco
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (String city : providedDensities.keySet()) {
            dataset.addValue(providedDensities.get(city), "Densidade (habitantes/km^2 total da cidade)", city);
            if (calculatedDensities.containsKey(city)) {
                dataset.addValue(calculatedDensities.get(city), "Densidade (habitantes/km^2 ocupado por prédios)", city);
            }
        }
        
        // Montagem do grafico
        JFreeChart barChart = ChartFactory.createBarChart(
                "Densidade de habitantes por área de prédios X Densidade Geral por Cidade",
                "Cidade",
                "Densidade (habitantes/km²)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(barChart);
        JFrame frame = new JFrame("City Density Comparison");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.add(chartPanel);
        frame.setVisible(true);
    }

    
}
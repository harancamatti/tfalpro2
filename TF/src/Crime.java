
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jxmapviewer.viewer.GeoPosition;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ricardo
 */
public class Crime {
    
    String time;
    GeoPosition coords;

    public Crime(List<String> line) {
        this.time = line.get(9);
        this.coords = new GeoPosition(Double.valueOf(line.get(11)), Double.valueOf(line.get(12)));
    }

    public static List<Crime> find(String faixaHorario, String diaSemana) {
        List<Crime> all = readCrimes("furtos.csv", faixaHorario, diaSemana);
        all.addAll(readCrimes("roubos.csv", faixaHorario, diaSemana));
        return all;
    }

    @Override
    public String toString() {
        return "Crime{" + "time=" + time + ", coords=" + coords + '}';
    }

    public static List<Crime> readCrimes(String fileName, String faixaHorario, String diaSemana) {
        Path path = Paths.get(".", fileName);
        try (Reader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"))) {
            BufferedReader bufReader = new BufferedReader(reader);
            Stream<String> lines = bufReader.lines().skip(1);
            Stream<List<String>> splittedLines = lines.map(line -> Arrays.asList(line.split(";")));
            Stream<List<String>> filteredLines = splittedLines.filter(line -> line.size() == 13);
            if (!diaSemana.trim().equals("TODOS")) {
                filteredLines = filteredLines.filter(line -> line.get(10).toLowerCase().contains(diaSemana.toLowerCase()));
            }
            if (!faixaHorario.trim().equals("TODOS")) {
                filteredLines = filteredLines.filter(line -> line.get(9).toLowerCase().contains(faixaHorario.toLowerCase()));
            }
            Stream<List<String>> cleanLines = filteredLines.filter(line -> !(line.get(9).trim().isEmpty() || line.get(11).trim().isEmpty() || line.get(12).trim().isEmpty()));
            Stream<Crime> crimeStream = cleanLines.map(splittedLine -> new Crime(splittedLine));
            List<Crime> crimes = crimeStream.collect(Collectors.<Crime> toList());
            return crimes;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
}

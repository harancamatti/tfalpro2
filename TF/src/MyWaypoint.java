

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

/**
 * Um waypoint que tem uma cor e um valor associado
 * @author Martin Steiger, Marcelo Cohen
 */
public class MyWaypoint extends DefaultWaypoint
{
	private Color color;
	private double value;
	private String name;

	/**
	 * @param color a cor
	 * @param coord a localização
	 */
	public MyWaypoint(List<String> wp) {
            super();
            this.name = wp.get(1);
            double latitude = Double.valueOf(wp.get(4));
            double longitude = Double.valueOf(wp.get(3));
            this.setPosition(new GeoPosition(latitude, longitude));
            this.color = Color.BLUE;
            this.value = 250;
        }
        
        public MyWaypoint calcCrime(List<Crime> crimes){
            this.value = crimes.stream().filter(crime -> algoritmos.AlgoritmosGeograficos.calcDistancia(getPosition(), crime.coords) < 3).collect(Collectors.counting());
            return this;
        }

    @Override
    public String toString() {
        return this.name;
    }

        
        public MyWaypoint(Color color, double value, GeoPosition coord)	{
		super(coord);
		this.color = color;
		this.value = value;
	}

	/**
	 * @returns a cor do waypoint
	 */
	public Color getColor()
	{
		return color;
	}
	
	public double getValue() {
		return value;
	}            
        
        public static List<MyWaypoint> find(String nomeRua){
            Path path = Paths.get(".", "taxis.csv");
            try (Reader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"))) {
                BufferedReader bufReader = new BufferedReader(reader);
                Stream<String> lines = bufReader.lines().skip(1);
                Stream<List<String>> splittedLines = lines.map(line -> Arrays.asList(line.split(";")));
                Stream<List<String>> filteredLines = splittedLines.filter(line -> line.size() == 5);
                if (!nomeRua.trim().isEmpty()) {
                    filteredLines = filteredLines.filter(line -> line.get(1).toLowerCase().contains(nomeRua.toLowerCase()));
                }
                Stream<List<String>> cleanLines = filteredLines.filter(line -> !(line.get(3).trim().isEmpty() || line.get(4).trim().isEmpty()));
                Stream<MyWaypoint> crimeStream = cleanLines.map(splittedLine -> new MyWaypoint(splittedLine));
                List<MyWaypoint> crimes = crimeStream.collect(Collectors.<MyWaypoint> toList());
                return crimes;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
}

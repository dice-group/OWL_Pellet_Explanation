# OWL Ontology Explanation Generator

This project allows users to generate explanations for OWL individuals based on specific queries using Pellet Reasoner. The explanations are generated in Manchester Syntax and saved as output files. It supports the use of ontology files in OWL format and allows users to query individuals based on specified conditions.

## Features
- Uses **Pellet Reasoner** to perform reasoning over OWL ontologies.
- Generates explanations for multiple OWL individuals using **Manchester Syntax**.
- Saves explanations to text files for future reference.
- Allows interactive selection list of individuals when given individual name doesn't match.
  
## Prerequisites
Before you can run the project, ensure you have the following installed:
- **Java 8+**
- **Maven** (for dependency management)
- **Pellet Reasoner** (included as a dependency via OWLAPI)
- **OWLAPI v3** (included as a dependency)

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Balramt/Pellet-Explanation.git
   ```

2. Install the required dependencies by adding them to your `pom.xml` (if using Maven):
   ```xml
   <dependency>
       <groupId>com.clarkparsia.pellet</groupId>
       <artifactId>pellet-owlapiv3</artifactId>
       <version>2.4.0</version>
   </dependency>
   <dependency>
       <groupId>net.sourceforge.owlapi</groupId>
       <artifactId>owlapi-distribution</artifactId>
       <version>3.5.2</version>
   </dependency>
   ```

## How to Use

1. Prepare your input file in the following format:
   ```
   ontology=path/to/your/ontology.owl
   ns=http://your-ontology-namespace#
   query=your query expression
   individuals=individual1,individual2
   ```
   - **ontology**: Full path to the local OWL ontology file.
   - **ns**: Namespace (IRI) of the ontology.
   - **query**: The class expression query in Manchester Syntax.
   - **individuals**: A comma-separated list of individual names to explain.

2. Run the application by providing the input file path:
   ```bash
   java -jar target/Ind_Explanation_IO_File.jar
   ```

3. You will be prompted to enter the path to the input file. Enter the full path to your input file:
   ```
   Enter the path to the input file: /path/to/your/input.txt
   ```

4. The program will generate explanations for the individuals matching the query. If an individual name cannot be found, the program will list all matching individuals, and you will be prompted to select one.

5. The explanations will be saved as `.txt` files in the `explanation_output` directory. Each file will be named based on the individual’s name and the current timestamp.

6. After the program completes, the console will output the location of the saved explanation file(s) and the total execution time.


## Contributing
If you find any bugs or have suggestions for new features, feel free to open an issue or a pull request.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

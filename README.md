# Pellet-Based Individual Explanation Tool for OWL Ontologies


```markdown
# Individual Explanation IO

This project provides a tool to generate explanations for individual instances in an OWL ontology using Pellet Reasoner and OWL API. The tool allows users to query an ontology for specific individuals and obtain detailed explanations of their classifications.

## Overview

The main functionality of this project is encapsulated in the `Individual_Explanation_IO` class. It uses PelletReasoner to load an OWL ontology and execute a query to find individuals that match a given OWL class expression. The tool then prompts the user to select an individual from the results and generates an explanation for the classification of the selected individual.

## How to Run the File

To run the `Individual_Explanation_IO` file, follow these steps:

1. **Prepare your environment:**
   - Ensure you have Java installed.
   - Download and include the required libraries (OWL API, Pellet, etc.) in your project.
   - Alternatively, if you are using an IDE, include the dependencies in your `pom.xml` file for Maven or the equivalent for other build       tools.
   

2. **Run the program:**
   - Compile and run the `Individual_Explanation_IO` Java file.
   - You will be prompted to enter the namespace, the local ontology file path, and the query string.
   - Note: Make sure to use the appropriate file path notation for your operating system 
### Example Commands:

To compile and run the program, you can use the following commands in your console:

```sh
# Compile the Java file
javac -cp ".;path/to/owlapi-distribution.jar;path/to/pellet.jar;path/to/other/dependencies/*" Individual_Explanation_IO.java

# Run the Java program
java -cp ".;path/to/owlapi-distribution.jar;path/to/pellet.jar;path/to/other/dependencies/*" Individual_Explanation_IO
```

### User Inputs:

- **Namespace:** The namespace of the ontology (e.g., `http://www.benchmark.org/family#`).
- **Local Ontology Path:** The local file path to the ontology (e.g., `E:/Workspace_Dice/DataSource/family.owl`).
- **Query String:** The OWL class expression query (e.g., `Female and hasChild some Male`).

### Program Execution:

1. **Namespace Input:**
   ```
   Enter the namespace without quotes (e.g., http://www.benchmark.org/family#):
   ```

2. **Ontology Path Input:**
   ```
   Enter the local ontology file path without quotes and replace / to (e.g., E:/Workspace_Dice/DataSource/family.owl):
   ```

3. **Query String Input:**
   ```
   Enter the query (e.g., Female and hasChild some Male):
   ```

4. **Select Individual for Explanation:**
   ```
   Individuals found:
   1: Individual1
   2: Individual2
   ...
   Enter the number corresponding to the individual you want an explanation for:
   ```

5. **Explanation Output:**
   The explanation for the selected individual will be printed in the console.

## License

This project is licensed under the MIT License.
```


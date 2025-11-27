## GBIF Spark Batch

This project contains the simple batch jobs that use Apache Spark. 
Common across all jobs is that they bring in little or no significant dependencies beyond core infrastructure components.

- [clustering](./README-clustering.md) generates the HBase table of relationships between occurrence records
- [fasta](./README-fasta.md) exports the distinct, cleaned sequences from occurrence records in a FASTA file 

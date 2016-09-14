--------------------------------
OLS 3.1.0 release notes
--------------------------------

- Added support for restricting searches using the API to hierarchical descendants of a term (i.e. all children including parts)
- Fixed bug where API term links for hierarchical children wasn’t working
- Updated UI widgets to use new standalone implementation based on BioJS. All UI components for search, graph and three view are in both NPM and BioJS
  - http://www.biojs.io/d/ols-autocomplete
  - http://www.biojs.io/d/ols-graphview
  - http://www.biojs.io/d/ols-treeview
- Improved handling of OBO DB Xrefs, these are now correctly formatted and have links to external databases where available (https://github.com/EBISPOT/OLS/issues/85, https://g\
ithub.com/EBISPOT/OLS/issues/83)
- Added support for rendering images e.g. http://www.ebi.ac.uk/ols/ontologies/uberon/terms?iri=http%3A%2F%2Fpurl.obolibrary.org%2Fobo%2FUBERON_0002530
- Fixed tree view bug where unsatisfiable classes were children of everything https://github.com/EBISPOT/OLS/issues/87
- Download ontology now has ontology name as filename https://github.com/EBISPOT/OLS/issues/84
- Updates to documentation
- Added EBIOLS twitter feed to homepage

--------------------------------
OLS 3.0.0 release notes
--------------------------------

We're pleased the announce the release of the new EMBL-EBI Ontology Lookup Service (OLS). You can access the new OLS at http://www.ebi.ac.uk/ols. The old service at http://www.ebi.ac.uk/ontology-lookup is now retired and will soon redirect to the new URL.

New features include
 - New ontology search and visualisation components
 - Includes the full set of OBO library ontologies and more
 - Supports ontologies published in both OBO and OWL
 - Ontology term history tracking
 - New RESTful API
   … more features to follow in the coming year!

For any future updates please subscribe to the OLS announce mailing lists https://listserver.ebi.ac.uk/mailman/listinfo/ols-announce

If there are any additional ontologies you would like to see in OLS or have suggestions for new features please get in touch using ols-support@ebi.ac.uk.

All the source code is also available on GitHub with instruction on how to build a local OLS. You can also post issues on the GitHub issue tracker if you prefer https://github.com/EBISPOT/OLS

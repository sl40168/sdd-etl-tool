## Phase IV: Extract **Xbond Trade** Data from COS for ETL

This is phase IV for ETL tool. In this phase, we need to extract **Xbond Trade** data from COS. COS(Cloud Object Storage) is a file system provided by Tencent, in which the files are stored in the sub directors of given bucket.

## I. Extract **Xbond Trade** data from COS

1. This extractor **MUST** follow the same principle as **Xbond Quote** Extractor in **Phase II**
2. It **MUST** extract data as its own extension of **SourceDataModel**

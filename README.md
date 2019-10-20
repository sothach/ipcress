# IP Address Compressor
## Deployment pipeline
[![Build Status](https://travis-ci.org/sothach/ipcress.png)](https://travis-ci.org/sothach/ipcress) >>
[![Coverage Status](https://coveralls.io/repos/github/sothach/ipcress/badge.svg?branch=master)](https://coveralls.io/github/sothach/ipcress?branch=master) >>
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a688282e09a04ddeb6d0b29f2c8b82e1)](https://www.codacy.com/project/sothach/ipcress/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=sothach/ipcress&amp;utm_campaign=Badge_Grade_Dashboard)

This project is a demo of Play framework / akka-streams

## The Problem
Given a list of IP address in quad-octet form, produce a digest with adrresses with consecutive octet #4 values replaced with a range.
The purpose of this is for white/blacklist application
Both a web Ui and REST API are provided by this service

## Example input

````text
99.243.64.40
99.243.64.41
99.243.64.42
99.244.106.35
99.244.121.59
99.244.121.60
99.244.121.61
99.244.156.149
````
A resulting digest will be produced:
````text
99.243.64.40-99.243.64.42
99.244.106.35
99.244.121.59-99.244.121.61
99.244.156.149
````
As well as plain text, JSON format may be specified:
````json
[
  {"ip": ["99.243.64.40", "99.243.64.42"]},
  {"ip": ["99.244.106.35"]},
  {"ip": ["99.244.121.59", "99.244.121.61"]},
  {"ip": ["99.244.156.149"]}
]
````

## Design
### Workflow: DigesterService

```scala
  def digestFromSource(request: Source[DigestRequest,_]): Future[Seq[Try[String]]] =
    request.async via filter via digester via formatter runWith Sink.seq
```
```textmate
+--------+    +--------+    +----------+    +-----------+    +----------+
| (1)    |    | (2)    |    | (3)      |    | (4)       |    | (5)      |
| source | => | filter | => | digester | => | formatter | => | response |
+--------+    +--------+    +----------+    +-----------+    +----------+
```
1.  Request body is parsed into a iteration over strings, wrapped in a `Source`
2.  Invalid (e.g., empty) elements are filtered out
3.  Digester algorithm applied to entire collection (needs to be sorted and unique)
4.  Results are formatted as per `Accepts` header / selection, JSON or Plain
5.  Response body set if results available

## Cloud Deployment
[see instructions](doc/deploy.md)

# Process URI

Process URI is a representation of tree hierarchy
of processes and cases. From version 6.2.0, the process
identifier from a single string has become a process URI, 
that is unique for process. E.g.:

```xml
<!-- Old form -->
<id>all_data</id>
```

```xml
<!-- New form -->
<id>data/all_data</id>
```

## Structure

The structure of URI is a tree, like a file system in a computer.
The parts of URI separated with slash ``/`` are called nodes.
A node is like a folder, that can contain processes, cases and other folders.

In the process identifier the last element is the name of the process,
from this last element no node is created. Each ``PetriNet`` object will remember their
origin node, using the ``uriNodeId`` attribute.

A case created from a process will have the same ``uriNodeId`` as the
process.

## Usage

It is then possible to search for processes and cases based on node and
URI path using the following functions:
- ``PetriNetService.findAllByUri(String uri)``
- ``WorkflowService.findAllByUri(String uri)``

This new form is used for generating dynamic 2-level menus on frontend.
There is a controller to get information about currently active nodes
and get the nodes a level above or below.




.. mageri documentation master file, created by
   sphinx-quickstart on Fri Aug  7 02:04:16 2015.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

MAGERI: Molecular tAgged GEnome Re-sequencing pIpeline
======================================================

.. figure:: _static/images/pipeline.png
    :align: center
    
Terminology
-----------

- UMI - unique molecular identifier, a short (typically 6-20nt) degenerate nucleotide sequence, that is attach to cDNA/DNA molecules in order to trace them throughout the entire experiment.
- Sample barcode - a short specific nucleotide sequence used to mark cDNA/DNA molecules that correspond to a given sample in a pooled sequencing library
- MIG - molecular identifier group, a set of reads or read pairs that have an identical UMI sequence
- MIG consensus - the consensus sequence of MIG, calculated from MIG position-weight matrix by taking the letter with highest frequency at each position
- CQS - consensus quality score, calculated as the maximal relative nucleotide frequency at a given PWM position. Usually scaled to [2, 40] range to fit Phred33 string representation. Indicates our confidence in the consensus sequence at a given position.
- Major variant (aka dominant variant, supermutant) - a sequence variant that is present in MIG consensus sequence
- Minor variant - a sequence variant that is present in reads within an MIG, but doesn’t get to the final MIG consensus sequence

.. toctree::
   :maxdepth: 2
   
   body

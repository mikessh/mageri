# basic sam file post-processing
for f in *.sam; do samtools view -bS $f > ${f%.sam}.bam; done
samtools merge project.bam *.bam
samtools sort -O bam -T tmp project.bam > project.sorted.bam
samtools index project.sorted.bam

# 
variants="$(ls *.vcf -m | tr -d '\n' | sed -e 's/, / --variant /g')"
java -jar GATK.jar -T CombineVariants -R reference.fasta $variants -o project.vcf -genotypeMergeOptions UNIQUIFY
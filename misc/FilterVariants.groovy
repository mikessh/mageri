/*
 * Copyright (c) 2014-2015, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact the Inventors using one of the following
 * email addresses: chudakovdm@mail.ru, chudakovdm@gmail.com
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */

def cli = new CliBuilder(usage: "cat variants.vcf | FilterVariants.groovy")
cli.f(args: 1, argName: "file", required: true, "Variant list in form chr(tab)coord(tab)ref(tab)alt")
cli.i("Invert filter")
cli.e("Extended output")

def opt = cli.parse(args)

if (opt == null) {
    System.exit(-1)
}

def variants = new HashSet<>(new File((String) opt.f).readLines().collect { it.split("[\t ]+")[0..3].join("\t") })
def negative = (boolean) opt.n, extendedOutput = (boolean) opt.e
def fields = [0, 1, 3, 4]

System.in.readLines().each {
    if (it.startsWith("#") && !extendedOutput) {
        System.out.println(it)
    } else {
        if (negative != variants.contains(it.split("[\t ]+")[fields].join("\t"))) {
            System.out.println(extendedOutput ? "+\t" + it : it)
        } else if (extendedOutput) {
            System.out.println("-\t" + it)
        }
    }
}
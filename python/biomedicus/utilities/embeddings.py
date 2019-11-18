# Copyright 2019 Regents of the University of Minnesota.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import io
from argparse import ArgumentParser

from pathlib2 import Path


def load_vectors(fname):
    with Path(fname).open('r', encoding='utf-8', newline='\n', errors='ignore') as fin:
        n, d = map(int, fin.readline().split())
        words = []
        vectors = []
        for line in fin:
            tokens = line.rstrip().split(' ')
            words.append(tokens[0])
            vectors.append(list(map(float, tokens[1:])))
    return words, vectors


def main(args=None):
    parser = ArgumentParser()
    parser.add_argument('input_file')
    parser.add_argument('output_file')
    config = parser.parse_args(args)
    words, vectors = load_vectors(config.input_file)

    with open(config.output_file, 'w') as f:
        f.writelines(words)


if __name__ == '__main__':
    main()
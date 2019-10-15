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
import os
import subprocess
from pathlib import Path

import pytest
from mtap import Pipeline, RemoteProcessor, EventsClient, LocalProcessor
from mtap.io.serialization import JsonSerializer
from mtap.metrics import Metrics, Accuracy
from mtap.utils import find_free_port


@pytest.fixture(name='sentences_service')
def fixture_sentences_service(events_service, processor_watcher):
    port = str(find_free_port())
    address = '127.0.0.1:' + port
    p = subprocess.Popen(['python', '-m', 'biomedicus.sentences',
                          'processor',
                          '-p', port,
                          '--events', events_service],
                         start_new_session=True, stdin=subprocess.PIPE,
                         stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    yield from processor_watcher(address, p)


@pytest.mark.phi_test_data
@pytest.mark.performance
def test_sentence_performance(events_service, sentences_service):
    input_dir = Path(os.environ['BIOMEDICUS_TEST_DATA']) / 'sentences'

    accuracy = Accuracy()
    with EventsClient(address=events_service) as client, Pipeline(
            RemoteProcessor(processor_id='biomedicus-sentences', address=sentences_service),
            LocalProcessor(Metrics(accuracy, tested='sentences', target='Sentence'),
                           component_id='metrics', client=client)
    ) as pipeline:
        for test_file in input_dir.glob('**/*.json'):
            with JsonSerializer.file_to_event(test_file, client=client) as event:
                document = event.documents['plaintext']
                results = pipeline.run(document)
                print('Accuracy for event - ', event.event_id, ':', results[1].results['accuracy'])

        print('Accuracy:', accuracy.value)
        pipeline.print_times()
        assert accuracy.value > 0.7
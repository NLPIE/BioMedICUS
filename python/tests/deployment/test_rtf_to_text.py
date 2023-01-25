#  Copyright 2023 Regents of the University of Minnesota.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
import sys
import threading
import traceback
from pathlib import Path
from subprocess import Popen, PIPE, STDOUT, call
from tempfile import TemporaryDirectory

import pytest


@pytest.fixture(name='deploy_rtf_to_text')
def fixture_deploy_rtf_to_text():
    p = None
    listener = None
    try:
        p = Popen([sys.executable, '-m', 'biomedicus', 'deploy-rtf-to-text'], stdout=PIPE, stderr=STDOUT)
        e = threading.Event()

        def listen():
            print("Starting listener")
            for line in p.stdout:
                line = line.decode()
                print(line, end='', flush=True)
                if 'Done deploying all servers.' in line:
                    e.set()
            p.wait()
            e.set()

        listener = threading.Thread(target=listen)
        listener.start()
        e.wait()
        if p.returncode is not None:
            raise ValueError("Failed to deploy.")
        yield p
    finally:
        try:
            if p is not None:
                p.terminate()
                if listener is not None:
                    listener.join(timeout=5.0)
                    if listener.is_alive():
                        p.kill()
                        listener.join()
        except:
            print("Error cleaning up deployment")
            traceback.print_exc()


@pytest.mark.integration
def test_deploy_run_rtf_to_text(deploy_rtf_to_text):
    print("testing deployment")
    with TemporaryDirectory() as tmpdir:
        input_dir = str(Path(__file__).parent / 'rtf_in')
        code = call([sys.executable, '-m', 'biomedicus_client', 'run-rtf-to-text', input_dir, '-o', tmpdir],
                    timeout=30.0, stdout=STDOUT, stderr=STDOUT)
        assert code == 0
        assert (Path(tmpdir) / '97_204.rtf.txt').exists()

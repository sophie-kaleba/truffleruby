import re

from rebench.interop.rebench_log_adapter   import RebenchLogAdapter, OutputNotParseable
from rebench.model.data_point  import DataPoint
from rebench.model.measurement import Measurement

class SplittingStatsAdapter(RebenchLogAdapter):

    split_stat_file = "splitting_statistics.log"
    num_split_features = 10

    def __init__(self, include_faulty, executor):
        super(SplittingStatsAdapter, self).__init__(include_faulty, executor)

    def parse_data(self, data, run_id, invocation):
        iteration = 1
        data_points = []
        current = DataPoint(run_id)

        for line in data.split("\n"):
            if self.check_for_error(line):
                raise ResultsIndicatedAsInvalid(
                    "Output of bench program indicated error.")

            measure = None
            match = self.re_log_line.match(line)
            if match:
                time = float(match.group("runtime"))
                if match.group("unit") == "u":
                    time /= 1000
                criterion = (match.group(2) or 'total').strip()

                measure = Measurement(invocation, iteration, time, 'ms', run_id, criterion)

                split_result = self.fetch_and_parse_split_data(run_id, invocation, iteration, match.group(1))

                for split_measure in split_result:
                    current.add_measurement(split_measure)
            else:
                match = self.re_extra_criterion_log_line.match(line)
                if match:
                    value = float(match.group("value"))
                    criterion = match.group("criterion")
                    unit = match.group("unit")

                    measure = Measurement(invocation, iteration, value, unit, run_id, criterion)

            if measure:
                current.add_measurement(measure)

                if measure.is_total():
                    data_points.append(current)
                    current = DataPoint(run_id)
                    iteration += 1

        if not data_points:
            raise OutputNotParseable(data)
        
        return data_points

    def fetch_and_parse_split_data(self, run_id, invocation, iteration, benchmark):
        all_results = []
        with open(self.split_stat_file) as file:
            for line in (file.readlines()[-self.num_split_features-1:]):
                if "FROMBENCH" in line:
                    assert line.split("_")[1] == benchmark
                else:
                    current = line.split(":")
                    split_measure = Measurement(invocation, iteration, current[1].strip(), "nodes", run_id, current[0].strip())
                    all_results.append(split_measure)
        return(all_results)

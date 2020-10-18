import subprocess


def exec_command(map_file, parallelism):
    method = "sequential" if parallelism is None else "parallel-" + str(parallelism)
    return "/home/tom/ownCloud/uni/java/jdk-13.0.1/bin/java -javaagent:/home/tom/ownCloud/uni/java/idea-IC-192.7142.36/lib/idea_rt.jar=35971:/home/tom/ownCloud/uni/java/idea-IC-192.7142.36/bin -Dfile.encoding=UTF-8 -classpath /home/tom/uniproj/conprog/amazed_lab/out/production/amazed_lab amazed.Main {} {} -1".format(map_file, method)


def time_for(map_name, parallelism):
    result = subprocess.run(
        exec_command("maps/{}.map".format(map_name), parallelism),
        shell=True,
        capture_output=True,
    )

    if result.returncode != 0:
        print(result.stderr.decode("utf8"))
        raise Exception("!!")

    try:
        found_line, time = result.stdout.splitlines()
        assert found_line == b"Goal found :-D"

        solving, time, n, ms = time.split()
        assert (solving, time, ms) == (b"Solving", b"time:", b"ms")

        return float(n)
    except ValueError:
        print(exec_command("maps/{}.map".format(map_name), parallelism))
        print(result.stdout.decode("utf8"))
        raise


def average(map_name, parallelism, runs):
    total = 0
    for i in range(runs):
        n = time_for(map_name, parallelism)
        total += n
    return total / runs


MAPS = ("small", "169medium", "medium", "custom", "large")

if __name__ == '__main__':
    for parallelism in (None, 0, 1, 2, 3, 4, 5, 7, 10, 15, 20):
        print('| %s | ' % parallelism +
              ' | '.join(str(average(name, parallelism, 10)) for name in MAPS) +
              ' |')

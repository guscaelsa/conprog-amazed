import subprocess

command = "/home/tom/ownCloud/uni/java/jdk-13.0.1/bin/java -javaagent:/home/tom/ownCloud/uni/java/idea-IC-192.7142.36/lib/idea_rt.jar=35971:/home/tom/ownCloud/uni/java/idea-IC-192.7142.36/bin -Dfile.encoding=UTF-8 -classpath /home/tom/uniproj/conprog/amazed_lab/out/production/amazed_lab amazed.Main"


def exec_command(map_file, parallelism):
    method = "sequential" if parallelism is None else "parallel-" + str(parallelism)
    return command + " {} {} -1".format(map_file, method)


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
        # assert found_line == b"Goal found :-D", found_line

        solving, time, n, ms = time.split()
        assert (solving, time, ms) == (b"Solving", b"time:", b"ms")

        return float(n)
    except ValueError:
        print(exec_command("maps/{}.map".format(map_name), parallelism))
        print(result.stdout.decode("utf8"))
        raise


def record(settings, time):
    with open("results.txt", 'a') as f:
        f.write("| {c:<10} | {m:<10} | {p:>5} | {t:>10} ms |\n".format(
            c=settings["computer"],
            m=settings["map_name"] + ".map",
            p=settings["parallelism"],
            t=time,
        ))


def run(settings, repeats):
    for _ in range(repeats):
        parallelism = settings["parallelism"]
        parallelism = None if parallelism == "-" else int(parallelism)
        t = time_for(settings["map_name"], parallelism)
        print(t)
        record(settings, t)


def loop():
    settings = {
        "map_name": "unset",
        "parallelism": "unset",
        "computer": "unset",
    }
    while True:
        try:
            cmd = input("→ ")
            if "=" in cmd:
                name, val = cmd.split("=")
                name = name.strip()
                val = val.strip()
                assert name in settings
                settings[name] = val
            elif cmd == "run":
                run(settings, 1)
            elif cmd.startswith("run"):
                _, n = cmd.split()
                run(settings, int(n))
        except Exception as e:
            print(e)
        except KeyboardInterrupt:
            print("<-quit->")
            break


# MAPS = ("small", "169medium", "medium", "custom", "large")
MAPS = ["vast"]

if __name__ == '__main__':
    loop()

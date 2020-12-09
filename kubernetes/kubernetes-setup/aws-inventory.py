#! /usr/bin/python3.6
import boto3, sys, json, getopt




def main(argv):
    opts = dict(getopt.getopt(argv[1:], 'h', ['list', 'start', 'stop', 'help'])[0])
    if '--help' in opts or '-h' in opts or len(opts) == 0:
        print('{} [--help][--list][--start][--stop]'.format(argv[0]))
        exit(0)

    ec2 = boto3.resource('ec2')
    instances = ec2.instances.all()
    hosts = get_addresses(instances)

    if '--list' in opts:
        inventory = aws_inventory(hosts)
        print(json.dumps(inventory, indent=2))

    if '--start' in opts:
        aws_start(instances)

    if '--stop' in opts:
        aws_stop(instances)


def aws_start(instances):
    for instance in instances:
        for tag in instance.tags:
            if tag['Key'] == 'type' and tag['Value'] == 'kubernetes':
                instance.start()

def aws_stop(instances):
    for instance in instances:
        for tag in instance.tags:
            if tag['Key'] == 'type' and tag['Value'] == 'kubernetes':
                instance.stop()

def get_addresses(instances) -> ({'master': [...], 'worker': [...]}):
    master = []
    worker = []
    for instance in instances:
        if instance.state['Name'] != 'running':
            continue
        for tag in instance.tags:
            if tag['Key'] == 'kubernetes.type':
                if tag['Value'] == 'master':
                    master.append(instance)
                if tag['Value'] == 'worker':
                    worker.append(instance)
    return {'master': master, 'worker': worker}


def aws_inventory(hosts: {'master': [...], 'worker': [...]}):

    group_vars = {
        'ansible_ssh_common_args': '-o StrictHostKeyChecking=no',
        'ansible_ssh_private_key_file': '~/.ssh/aws/belenot.pem',
        'ansible_user': 'ubuntu'
    }
    master_group = {'hosts': [host.public_ip_address for host in hosts['master']], 'vars': group_vars}
    worker_group = {'hosts': [host.public_ip_address for host in hosts['worker']], 'vars': group_vars}
    hostvars = dict([ (host.public_ip_address, {'node_ip': host.private_ip_address}) for host in (hosts['master'] + hosts['worker'])])

    return {
        'master': master_group,
        'worker': worker_group,
        '_meta': {
            'hostvars': hostvars
        }
    }

if __name__ == '__main__':
    main(sys.argv)
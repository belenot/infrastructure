#! /usr/bin/python3.6
import boto3, sys, json

ec2 = boto3.resource('ec2')
instances = ec2.instances.all()


def main(argv):
    
    hosts = get_addresses()
    inventory = aws_inventory(hosts)
    print(json.dumps(inventory, indent=2))


def get_addresses() -> ({'master': [...], 'worker': [...]}):
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
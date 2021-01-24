#! /usr/bin/python3
import boto3, sys, json, getopt, socket

types = ['dns', 'edge', 'aw', 'kubernetes-master', 'kubernetes-worker', 'website', 'postgresql']

def main(argv):
    opts = dict(getopt.getopt(argv[1:], 'h', ['list', 'start', 'stop', 'help'])[0])
    if '--help' in opts or '-h' in opts or len(opts) == 0:
        print('{} [--help][--list][--start][--stop]'.format(argv[0]))
        exit(0)

    if '--list' in opts:
        inventory = inventory_list()
        print(json.dumps(inventory, indent=2))

    if '--start' in opts:
        aws_start()

    if '--stop' in opts:
        aws_stop()


def inventory_list():
    instances = boto3.resource('ec2').instances
    inventory = {'_meta': {'hostvars': {}}}
    common_vars = {
        'ansible_ssh_common_args': '-o StrictHostKeyChecking=no',
        'ansible_ssh_private_key_file': '~/.ssh/aws/belenot.pem',
        'ansible_user': 'ubuntu',
        'set_nameserver': False,
        'nameserver_ip': None
    }

    nameserver_ip = None

    for instance_type in types:
        for instance in instances.all():
            for tag in instance.tags:
                if tag['Key'] == 'type' and tag['Value'] == instance_type:
                    if instance_type not in inventory:
                        inventory[instance_type] = {'hosts': [], 'vars': {}}
                    if instance.public_ip_address:
                        inventory[instance_type]['hosts'].append(instance.public_ip_address)

    for group in inventory:
        if group == '_meta':
            continue
        for host in inventory[group]['hosts']:
            inventory['_meta']['hostvars'][host] = { **common_vars }

    for instance in instances.all():
        for tag in instance.tags:
            if tag['Key'] == 'type' and tag['Value'] in ['kubernetes-worker', 'kubernetes-master'] and instance.state['Name'] == 'running':
                inventory['_meta']['hostvars'][instance.public_ip_address]['node_ip'] = instance.private_ip_address

    if len(inventory['dns']['hosts']) > 0:
        hosts = [host for group in inventory if group not in ['_meta', 'dns'] for host in inventory[group]['hosts']]
        for host in hosts:
            nameserver_ip = instances_by_type(instances, 'dns')[0].private_ip_address
            vars = inventory['_meta']['hostvars'][host]
            vars['set_nameserver'] = True
            vars['nameserver_ip'] = nameserver_ip

        inventory['dns']['vars'] = {'domain_names': {}}
        domain_names = inventory['dns']['vars']['domain_names']
        domain_names['ns'] = [ x.private_ip_address for x in instances_by_type(instances, 'dns') ]
        domain_names['edge'] = [ x.private_ip_address for x in instances_by_type(instances, 'edge') ]
        domain_names['aw'] = [ x.private_ip_address for x in instances_by_type(instances, 'aw') ]
        domain_names['node-0.k8s'] = [x.private_ip_address for x in instances_by_type(instances, 'kubernetes-master')]
        domain_names['website'] = [x.private_ip_address for x in instances_by_type(instances, 'website')]
        domain_names['postgresql'] = [x.private_ip_address for x in instances_by_type(instances, 'postgresql')]
        kubernetes_workers = [ x.private_ip_address for x in instances_by_type(instances, 'kubernetes-worker') ]
        for i in range(0, len(kubernetes_workers)):
            domain_names['node-'+str(i+1)+'.k8s'] = [ kubernetes_workers[i] ]
        domain_names['nexus'] = [ x.private_ip_address for x in instances_by_type(instances, 'nexus') ]


    inventory['edge']['vars']['ignore_acme'] = False

    return inventory


def instances_by_type(instances, instance_type):
    result = []
    for instance in instances.all():
        for tag in instance.tags:
            if tag['Key'] == 'type' and tag['Value'] == instance_type and instance.state['Name'] == 'running':
                result.append(instance)
    return result

def aws_start():
    instances = boto3.resource('ec2').instances
    for instance in instances.all():
        for tag in instance.tags:
            if tag['Key'] == 'type' and tag['Value'] in types and instance.state['Name'] == 'stopped':
                instance.start()
#
def aws_stop():
    instances = boto3.resource('ec2').instances
    current_instance = None
    for instance in instances.all():
        for tag in instance.tags:
            if tag['Key'] == 'type' and tag['Value'] in types and instance.state['Name'] == 'running':
                if socket.gethostbyname(socket.gethostname()) == instance.private_ip_address:
                    current_instance = instance
                else:
                    instance.stop()
    if current_instance:
        current_instance.stop()

#
# def aws_inventory(hosts: {'master': [...], 'worker': [...]}):
#
#     group_vars = {
#         'ansible_ssh_common_args': '-o StrictHostKeyChecking=no',
#         'ansible_ssh_private_key_file': '~/.ssh/aws/belenot.pem',
#         'ansible_user': 'ubuntu'
#     }
#     master_group = {'hosts': [host.public_ip_address for host in hosts['master']], 'vars': group_vars}
#     worker_group = {'hosts': [host.public_ip_address for host in hosts['worker']], 'vars': group_vars}
#     hostvars = dict([ (host.public_ip_address, {'node_ip': host.private_ip_address}) for host in (hosts['master'] + hosts['worker'])])
#
#     return {
#         'master': master_group,
#         'worker': worker_group,
#         '_meta': {
#             'hostvars': hostvars
#         }
#     }

if __name__ == '__main__':
    main(sys.argv)

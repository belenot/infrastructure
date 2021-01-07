# Infrastructure as Code

This project describes infrastructure on AWS platform.

Terraform configuration describes resources on AWS.
Vagrant describes setup on local VMs, using VirtualBox provider.
Host configuration is done via Ansible roles.
Public key infrastructure is described in `/ca` directory.

## Local rollout

### Prerequisites

* 16GB RAM.
* Vagrant.
* VirtualBox.
* Free CIDRs on `192.168.50.0/24` - `192.168.54.0/24`.
* Free ports on 2200-2206.
* Password for decrypting ansible-vault secrets.

### Rollout
Run in shell command:

```shell script
vagrant up
```

### Configuration

After all VMs up run:

```shell script
ansible-playbook --vault-password-file ~/password playbook.yml -i vagrant-inventory.yaml --timeout 60
```

### Pause

```shell script
vagrant halt
```

### Cleanup

```shell script
vagrant destroy -f
```

## Rollout on AWS

### Prerequisites

* awscli logged in us-east-2 zone.
* Password for decrypting ansible-vault secrets.
* Terraform.
* Available credits on aws account.
* python3, boto3 python module.

### Rollout

Run in shell command:

```shell script
cd terraform
terraform apply
```

### Configuration

Terraform will ask for confirming plan. Type yes in stdin. After terraform will apply plan run:

```shell script
chmod +x aws-inventory.py
ansible-playbook --vault-password-file ~/password playbook.yml -i aws-inventory.py --timeout 60
```

### Pause

```shell script
./aws-inventory.py --pause
```

### Cleanup

```shell script
cd terraform
terraform destroy
```

Confirm plan by typing yes.

## Top-level architecture

Image made using draw.io.

![Infrastructure](infrastructure.jpg)
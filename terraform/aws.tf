terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "3.22.0"
    }
  }
  backend "s3" {
    bucket = "state-bucket.belenot.com"
    key    = "terraform.tfstate"
    region = "us-east-2"
  }
}

variable "key_pair_name" {
  default = "belenot"
}

data "aws_eip" "edge_eip" {
  tags = {
    type = "edge"
  }
}

provider "aws" {
  region = "us-east-2"
}

data "aws_ami" "ubuntu" {
  #   most_recent = true Commented, because causes recreation, when new ami exposed.
  owners = ["099720109477"] # Canonical
  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-focal-20.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }

  filter {
    name   = "image-id"
    values = ["ami-08541d54d54812a51"]
  }
}

// Created S3 Bucket initially, but after that used as datasource
//resource "aws_s3_bucket" "state_bucket" {
//  bucket = "state-bucket.belenot.com"
//  acl    = "private"
//  versioning {
//    enabled = true
//
//  }
//  tags = {
//    type = "bucket"
//  }
//}

//resource "aws_eip" "edge_eip" {
//  vpc        = true
//  instance   = aws_instance.edge.id
//  tags = {
//    type      = "edge"
//    generator = "terraform"
//  }
//}

resource "aws_eip_association" "edge_eip_association"{
//  instance_id = aws_instance.edge.id
  instance_id = aws_spot_fleet_request.fleet_requests.id
  allocation_id = data.aws_eip.edge_eip.id
}

resource "aws_vpc" "belenot" {
  cidr_block           = "172.31.0.0/16"
  enable_dns_hostnames = true
  tags = {
    generator = "terraform"
  }
}

resource "aws_route_table" "belenot" {
  vpc_id = aws_vpc.belenot.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.belenot.id

  }
}

resource "aws_route_table_association" "subnet1_route" {
  route_table_id = aws_route_table.belenot.id
  subnet_id      = aws_subnet.subnet1.id
}

resource "aws_route_table_association" "subnet2_route" {
  route_table_id = aws_route_table.belenot.id
  subnet_id      = aws_subnet.subnet1.id
}

resource "aws_internet_gateway" "belenot" {
  vpc_id = aws_vpc.belenot.id
  tags = {
    generator = "terraform"
  }
}

resource "aws_subnet" "subnet1" {
  cidr_block        = "172.31.0.0/20"
  vpc_id            = aws_vpc.belenot.id
  availability_zone = "us-east-2a"
  tags = {
    genertaor = "terraform"
  }

}

resource "aws_subnet" "subnet2" {
  cidr_block        = "172.31.16.0/20"
  vpc_id            = aws_vpc.belenot.id
  availability_zone = "us-east-2b"
  tags = {
    genertaor = "terraform"
  }
}

resource "aws_security_group" "alpha" {
  name        = "alpha"
  description = "Alpha: Internal subnet."
  vpc_id      = aws_vpc.belenot.id

  ingress {
    from_port        = 22
    protocol         = "tcp"
    to_port          = 22
    description      = "SSH for administration."
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  ingress {
    from_port   = 0
    protocol    = "-1"
    to_port     = 0
    self        = true
    description = "Allow traffic from alpha."
  }

  ingress {
    from_port       = 0
    protocol        = "-1"
    to_port         = 0
    description     = "Allow all traffic from edge."
    security_groups = [aws_security_group.edge.id]
  }

  egress {
    from_port   = 0
    protocol    = "-1"
    to_port     = 0
    description = "Allow any output traffic."
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "edge" {
  name        = "edge"
  description = "Edge: Ingress/Egress proxy."
  vpc_id      = aws_vpc.belenot.id

  ingress {
    from_port        = 22
    protocol         = "tcp"
    to_port          = 22
    description      = "SSH for administration."
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  ingress {
    from_port   = 6443
    protocol    = "tcp"
    to_port     = 6443
    description = "Kubernetes API Server."
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 80
    protocol    = "tcp"
    to_port     = 80
    description = "Ingress HTTP."
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    protocol    = "tcp"
    to_port     = 443
    description = "Ingress HTTPS."
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 8081
    protocol    = "tcp"
    to_port     = 8081
    description = "Nexus UI."
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 5000
    protocol    = "tcp"
    to_port     = 5000
    description = "Nexus docker registry."
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 8080
    protocol    = "tcp"
    to_port     = 8080
    description = "Jenkins."
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    protocol    = "-1"
    to_port     = 0
    description = "Allow any output traffic."
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_spot_fleet_request" "fleet_requests" {
  for_each = var.instances
  iam_fleet_role              = data.aws_iam_role.fleet_role.arn
  instance_interruption_behaviour = "stop"
  terminate_instances_with_expiration = true
  launch_specification {
    ami                         = data.aws_ami.ubuntu.id
    key_name                    = var.key_pair_name
    subnet_id                   = aws_subnet.subnet1.id

    instance_type               = each.value.instance_type
    vpc_security_group_ids      = [aws_security_group.alpha.id]
    associate_public_ip_address = each.value.associate_public_ip_address
    tags = {
      type      = each.key
      generator = "terraform"
    }
  }
  wait_for_fulfillment = true

  target_capacity = each.value.target_capacity
}

variable "instances" {
  default = {
    kubernetes-worker = {
      instance_type               = "t2.medium"
      associate_public_ip_address = true
      target_capacity             = 2
    }
    kubernetes-master = {
      instance_type               = "t2.medium"
      associate_public_ip_address = true
      target_capacity             = 1
    }
  }

}

data "aws_iam_role" "fleet_role" {
  name = "aws-ec2-spot-fleet-tagging-role"
}
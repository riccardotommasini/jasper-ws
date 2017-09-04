ENGINE=csparql

USER=streamreasoning

IMG_TAG=$(USER)/$(ENGINE)

all: run
clean:
	docker rm $(ENGINE)_running; true

build:
	docker rmi $(IMG_TAG); true
	docker build -t $(IMG_TAG) --build-arg ENGINE=$(ENGINE) .

run_csparql:
	docker rm $(ENGINE)_running; true
	docker run -it -p 8181:8181 -p 9000-9100:9000-9100 --name $(ENGINE)_running $(IMG_TAG)
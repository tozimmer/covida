#include "CovidaNativeInterface.h"
#include "algorithm.h"
#include <iostream>



void onImageOpened(const std::string& image_path)
{
	std::cout << "image opened: " << image_path << std::endl;
}


void onImageClosed(const std::string& image_path)
{
	std::cout << "image closed: " << image_path << std::endl;
}



void onVideoOpened(const std::string& image_path)
{
	std::cout << "video opened: " << image_path << std::endl;
}


void onVideoClosed(const std::string& image_path)
{
	std::cout << "video closed: " << image_path << std::endl;
}


void circleAnnotation(const string path, const string description, const long start, const long stop,
            const float dx, const float dy, const float radius,
			const vector<Point*> points)
{
	cout << "[path]:= " << path << " [description]:=" << description
		 << " [start]:= " << start << " [stop]:=" << stop
	     << " [dx]:= " << dx << " [dy]:=" << dy << " [radius]:=" << radius;
}

void lineAnnotation(const string path, const string description, const long start, const long stop,
            const float bx, const float by, const float ex, const float ey,
            const vector<Point*> points)
{
	cout << "[path]:= " << path << " [description]:=" << description
		 << " [start]:= " << start << " [stop]:=" << stop;
}

void polygonAnnotation(const string path, const string description, const long start, const long stop,
            const vector<Point*> points)
{
	cout << "[path]:= " << path << " [description]:=" << description
		 << " [start]:= " << start << " [stop]:=" << stop;
}
/*
 * This file is part of the CoVIda project.
 * Copyright (C) 2013 DFKI GmbH. All rights reserved.
 *
 * Disclaimer:
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND
 * CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
#include <string>
#include <vector>

using namespace std;

/** 
 * \brief Datastructure to store the points of the stroke.
 * The points can be store in an absolute or relative [0.-1.] manner.
 */
typedef struct Point {
	float x;
	float y;
	Point(float _x, float _y) : x(_x), y(_y) {};
} Point;
 
/**
 * \brief Method is called whenever an image is opened by CoVida application.
 * \param absolute path to the image 
 */
void onImageOpened(const string& image_path);

/**
 * \brief Method is called whenever an image is closed by CoVida application.
 * \param absolute path to the image 
 */
void onImageClosed(const string& image_path);

/**
 * \brief Method is called whenever an image is opened by CoVida application.
 * \param absolute path to the image 
 */
void onVideoOpened(const string& video_path);

/**
 * \brief Method is called whenever an image is closed by CoVida application.
 * \param absolute path to the image 
 */
void onVideoClosed(const string& video_path);
/**
 * \brief Called whenever a circle is drawn in the application.
 * \param path - path to the image/video
 * \param description - annotation string 
 * \param start - if the annotated object is a video start is the starting frame of the annotation
 * \param end - if the annotated object is a video end is the end frame of the annotation
 * \param dx - x coordinate center point of circle
 * \param dy - y coordinate center point of circle
 * \param radius - radius of the circle
 * \param points - vector with raw points
 */
void circleAnnotation(const string path, const string description, const long start, const long stop,
            const float dx, const float dy, const float radius,
			const vector<Point*> points);
/**
 * \brief Called whenever a line is drawn in the application.
 * \param path - path to the image/video
 * \param description - annotation string 
 * \param start - if the annotated object is a video start is the starting frame of the annotation
 * \param end - if the annotated object is a video end is the end frame of the annotation
 * \param dx - x coordinate center point of circle
 * \param dy - y coordinate center point of circle
 * \param radius - radius of the circle
 * \param points - vector with raw points
 */
void lineAnnotation(const string path, const string description, const long start, const long stop,
            const float bx, const float by, const float ex, const float ey,
            const vector<Point*> points);
/**
 * \brief Called whenever a polygon is drawn in the application.
 * \param path - path to the image/video
 * \param description - annotation string 
 * \param start - if the annotated object is a video start is the starting frame of the annotation
 * \param end - if the annotated object is a video end is the end frame of the annotation
 * \param dx - x coordinate center point of circle
 * \param dy - y coordinate center point of circle
 * \param radius - radius of the circle
 * \param points - vector with raw points
 */
void polygonAnnotation(const string path, const string description, const long start, const long stop,
            const vector<Point*> points);
